package com.hawker.canal.binlog.dump;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.hawker.canal.CanalInstance;
import com.hawker.canal.CanalTable;
import com.hawker.canal.binlog.utils.BinlogParser;
import com.hawker.service.ApplyService;
import com.hawker.service.KafkaProducerService;
import com.hawker.service.KafkaTopicManager;
import fj.Ord;
import fj.data.List;
import fj.data.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class PullManager {

    private final static Logger logger = LoggerFactory.getLogger("pullManager");

    @Value("${canal.host}")
    private String canalHost;

    @Value("${canal.port}")
    private String canalPort;

    @Autowired
    private KafkaProducerService msgProducer;

    @Autowired
    private ApplyService applyService;

    // 维护了任务队列instance
    private BlockingQueue<CanalInstance> instanceQueue = new LinkedBlockingQueue<>();
    // 维护了当前队列中的instance 和 ip 的映射关系
    private ConcurrentMap<String, CanalInstance> activeInstanceIpMap = new ConcurrentHashMap<>();
    // 维护了需要修改的instance 和 ip 的映射关系
    private ConcurrentMap<String, CanalInstance> modifiedInstanceIpMap = new ConcurrentHashMap<>();
    // 维护了需要删除的instance ip
    private Set<String> removedInstanceIpSet = new ConcurrentSkipListSet<>();

    private static final int batchSize = 5 * 1024;
    private static final int CORE_NUM = Runtime.getRuntime().availableProcessors() * 2;
    private ExecutorService pullThreadPool = Executors.newFixedThreadPool(CORE_NUM);


    @PostConstruct
    public void init() {
        logger.info("pullManager 开始初始化，canal地址：" + canalHost + "：" + canalPort);

        List<CanalTable> initList = fj.data.List.iterableList(applyService.applyInstanceList());
        // 去除重复（因为有不同的用户监听同一张库同一张表）
        List<CanalTable> cts = initList.nub(CanalTable.canalTableOrd);
        List<CanalInstance> cis = shuffle(cts);
        cis.forEach(ci -> {
            if (ci.needFullPull()) {
                putIntoQueue(ci);
                logger.info("新增了[{}]条全量dump, ip:{}, table: {}", ci.getFullDumpList().size(), ci.getIp(), ci.getFullDumpStr());
            } else {
                initTask(ci);
            }
        });

        start();
    }

    /**
     * 初始化读取数据库监听表时，洗牌操作
     * 1: 把 canalTables 按照ip 分组组装 delta CanalInstance
     * 2: 把 canalTables 按照ip+db 分组组装 full dump CanalInstance
     */
    private List<CanalInstance> shuffle(List<CanalTable> cts) {

        TreeMap<String, List<CanalTable>> ipGroup = cts.groupBy(CanalTable::getIp, Ord.stringOrd);
        List<CanalInstance> deltaInstances = ipGroup.values().map(fjCis -> {
            CanalInstance c = CanalInstance.from(fjCis.head());
            c.setFilterList(fjCis.map(CanalTable::getFilter).toJavaList());
            return c;
        });


        TreeMap<String, List<CanalTable>> ipDBGroup = cts.groupBy(CanalTable::getIP_DB, Ord.stringOrd);
        List<CanalInstance> fullInstances = ipDBGroup.values().map(fjCis -> {
            List<CanalTable> fullTable = fjCis.filter(CanalTable::shouldFullPull);

            if (fullTable.isNotEmpty()) {
                CanalInstance c = CanalInstance.from(fullTable.head());
                c.setFullDumpList(fullTable.map(CanalTable::getFilter).toJavaList());
                return c;
            } else {
                return null;
            }
        }).filter(a -> a != null);

        return deltaInstances.append(fullInstances);
    }


    /**
     * 初始化canal client connection, 然后放入blockQueue中
     */
    private void initTask(CanalInstance instance) {
        CanalInstance connInstance = setConnection(instance);
        logger.info("新增了1个Canal filter, [{}]张表, ip: {}, filter: {}", connInstance.getFilterList().size(),
                connInstance.getIp(), connInstance.getFilter());

        putIntoQueue(connInstance);
        activeInstanceIpMap.putIfAbsent(instance.getIp(), connInstance);
    }

    private CanalInstance setConnection(CanalInstance instance) {
        String destination = instance.getDestination();
        CanalConnector connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(canalHost, Integer.parseInt(canalPort)), destination, "", "");
        connector.connect();

        connector.subscribe(instance.getFilter());
        instance.setCanalConn(connector);

        return instance;
    }


    public void addCanalTable(CanalTable t) {
        if (t.shouldFullPull()) {
            CanalInstance i = CanalInstance.from(t);
            i.appendFullDumpTable(t.getFilter());
            putIntoQueue(i);
        }

        String ip = t.getIp();
        if (activeInstanceIpMap.containsKey(ip)) {
            CanalInstance i = activeInstanceIpMap.get(ip);
            if (i.appendFilter(t.getFilter())) {
                modifiedInstanceIpMap.put(ip, i);
            }
        } else {
            // TODO 如果初次监听数据库服务器，需要配置canal client configuration， 应该为自动配置.
            CanalInstance i = CanalInstance.from(t);
            i.appendFilter(t.getFilter());
            initTask(i);
        }
    }

    public void removeCanalTable(CanalTable t) {
        String ip = t.getIp();
        if (activeInstanceIpMap.containsKey(ip)) {
            CanalInstance i = activeInstanceIpMap.get(ip);
            i.removeFilter(t.getFilter());

            if (i.readyToRemoveInstance()) {
                removedInstanceIpSet.add(t.getIp());
            } else {
                modifiedInstanceIpMap.put(t.getIp(), i);
            }
        }
    }


    private void executeFullDump(CanalInstance instance) {
        logger.info("开始执行full dump task, {} - {}" + instance.getIp(), instance.getFullDumpStr());
        FullPullThread task = new FullPullThread(instance);
        pullThreadPool.submit(task);
    }

    private void executeDeltaDump(CanalInstance instance) {
        logger.info("开始执行delta dump task, {} - {}", instance.getIp(), instance.getFilter());
        DeltaDumpThread task = new DeltaDumpThread(instance);
        pullThreadPool.submit(task);
    }

    private void start() {
        new Thread(() -> {
            try {
                while (true) {
                    CanalInstance instance = instanceQueue.poll();
                    if (instance != null) {
                        if (instance.needFullPull())
                            executeFullDump(instance);
                        else
                            executeDeltaDump(instance);
                    }

                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                logger.error("process error!", e);
            } finally {
                pullThreadPool.shutdown();

                CanalInstance instance;
                while ((instance = instanceQueue.poll()) != null) {
                    instance.getCanalConn().disconnect();
                }
            }
        }).start();
    }

    /**
     * 执行全量pull任务线程, 拉取完之后不会放回到 blockQueue中
     */
    private class FullPullThread implements Runnable {
        private CanalInstance instance;

        FullPullThread(CanalInstance instance) {
            this.instance = instance;
        }

        private void fullPull() {
            String ip = instance.getIp();
            String port = instance.getPort();
            java.util.List<String> fullDump = instance.getFullDumpList();

            logger.info("full pull list:" + instance.getFullDumpStr());

            for (String db_tb : fullDump) {
                String db = db_tb.split("\\.")[0];
                String tb = db_tb.split("\\.")[1];

                String topic = KafkaTopicManager.getTopic(ip, port, db, tb);
                Connection conn = null;

                try {
                    String sql = "select * from " + tb;
                    String driver = "com.mysql.jdbc.Driver";
                    String url = "jdbc:mysql://" + ip + ":" + port + "/" + db;
                    String user = instance.getUsr();
                    String password = instance.getPsword();

                    logger.info("Full pull, DataBase: {},  sql: {}", url, sql);

                    Class.forName(driver);
                    conn = DriverManager.getConnection(url, user, password);
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();

                    int n = 0;
                    while (rs.next()) {
                        ResultSetMetaData data = rs.getMetaData();

                        // 查询并封装结果:数据源id、db、tb、type(select)
                        JSONObject resultJson = new JSONObject();
                        resultJson.put("dbName", db);
                        resultJson.put("tableName", tb);
                        resultJson.put("eventType", "INSERT");
                        resultJson.put("sourceType", "MYSQL");
                        resultJson.put("ip", ip);
                        resultJson.put("port", port);
                        resultJson.put("executeTime", new Date().getTime());

                        JSONObject rowJson = new JSONObject(true);
                        for (int i = 1; i <= data.getColumnCount(); i++) {
                            String columnName = data.getColumnName(i);
                            String columnValue = rs.getString(i);
                            rowJson.put(columnName, columnValue);
                        }
                        resultJson.put("afterRow", rowJson);

                        msgProducer.send(topic, resultJson.toString());
                        n++;
                    }
                    logger.info("发送全量记录完成：{}, 个数: {}", topic, n);
                    rs.close();
                    stmt.close();
                } catch (Exception e) {
                    logger.error("Full pull error happened", e);
                } finally {
                    try {
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        logger.error("close db connection error", e);
                    }
                }
            }
        }

        @Override
        public void run() {
            // 全量拉取前无需增加 preCheck 判断
            fullPull();
            setPulledFlag();
        }

        // 设置full_pulled 标志为已经拉取
        private void setPulledFlag() {
            final String ip = instance.getIp();

            instance.getFullDumpList().forEach(dbTable -> {
                String db = dbTable.split("\\.")[0];
                String table = dbTable.split("\\.")[1];
                int n = applyService.setToFullPulled(ip, db, table);

                if (n == 1) logger.info("全量拉取状态回写成功, ip: {}, dataBase: {}, table: {}", ip, db, table);
            });
        }
    }

    private void putIntoQueue(CanalInstance instance) {
        try {
            instanceQueue.put(instance);
        } catch (InterruptedException e) {
            logger.error("Put canal instance back into block queue error", e);
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * 判断当前instance是否已修改或已删除。
     * 1. 若已修改，则终止当前任务，并把修改的instance放回队列;
     * 2. 若已删除，则直接关闭连接，终止当前任务;
     */
    private boolean preCheck(CanalInstance instance) {
        String ip = instance.getIp();
        String preFilter = instance.getFilter();

        if (modifiedInstanceIpMap.containsKey(ip)) {
            // 重置connector
            instance.getCanalConn().rollback();
            CanalInstance newInstance = modifiedInstanceIpMap.get(ip);
            instance.setFilterList(newInstance.getFilterList());

            instance.getCanalConn().subscribe(instance.getFilter());

            putIntoQueue(instance);

            modifiedInstanceIpMap.remove(ip);
            logger.info("instance被修改，当前任务终止, 重新放回队列中, canal ip: {}, previous filter: {}, now filter: {}",
                    ip, preFilter, instance.getFilter());
            return false;
        }

        if (removedInstanceIpSet.contains(ip)) {
            instance.getCanalConn().rollback();
            removedInstanceIpSet.remove(ip);
            activeInstanceIpMap.remove(ip);
            logger.info("instance被删除，任务终止 canal ip: {}, filter: {}", ip, instance.getFilter());
            return false;
        }

        return true;
    }

    /**
     * 增量监听canal server 任务，调用完之后 放回blockQueue中
     */
    private class DeltaDumpThread implements Runnable {
        private CanalInstance instance;

        DeltaDumpThread(CanalInstance instance) {
            this.instance = instance;
        }

        /**
         * 拉取消息并发布，消息格式：
         */
        private void deltaPull() {
            String ip = instance.getIp();
            String port = instance.getPort();
            String filter = instance.getFilter();

            try {
                Message message = null;
                try {
                    message = instance.getCanalConn().getWithoutAck(batchSize);
                } catch (CanalClientException ioe) {
                    // FIX JIRA(http://139.224.239.206:8080/browse/DSJBM-9)
                    // 如果是io异常，则说明canal connection失效，所以重新建立connection
                    if (ioe.getCause() instanceof IOException) {
                        instance = setConnection(instance);
                        logger.warn("instance reconnection, ip: {}, filter: {}", ip, filter);
                        return;
                    }
                }

                if (null == message) return;

                long batchId = message.getId();
                int size = message.getEntries().size();

                if (batchId != -1 && size != 0) {
                    logger.info("收到binlog消息，{} - {}", ip, filter);
                    JSONArray changes = BinlogParser.getBinlogMsg(message);

                    for (int i = 0; i <= changes.size() - 1; i++) {
                        JSONObject change = (JSONObject) changes.get(i);
                        change.put("ip", ip);
                        change.put("port", port);
                        String db = change.getString("dbName");
                        String tb = change.getString("tableName");

                        String topic = KafkaTopicManager.getTopic(ip, port, db, tb);
                        msgProducer.send(topic, change.toString());
                        logger.info("get new event, topic: {}, content: {}", topic, change.toString());
                    }
                } else {
                    logger.debug("无binlog消息，{} - {}", ip, filter);
                }

                instance.getCanalConn().ack(batchId); // 提交确认
            } catch (CanalClientException e) {
                logger.error("canal get binlog event error, filter: {}", filter, e);
            }
        }

        @Override
        public void run() {
            if (preCheck(instance)) {
                deltaPull();
                putIntoQueue(instance);
            }
        }
    }
}
