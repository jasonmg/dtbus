package com.hawker.service.impl;

import com.hawker.config.FlumeAgentConfig;
import com.hawker.pojo.Authentication;
import com.hawker.pojo.LogApplyVO;
import com.hawker.pojo.Node;
import com.hawker.pojo.Result;
import com.hawker.service.LogApplyService;
import com.hawker.service.SSH2Service;
import com.trilead.ssh2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangliping on 2017/9/19.
 */
@Service
public class SSH2ServiceImpl implements SSH2Service {
    @Value("${flume.local.default}")
    String localFlumeTar;
    @Value("${flume.remote.default}")
    String remoteFlumeDir;
    protected final static Logger logger = LoggerFactory.getLogger(SSH2ServiceImpl.class);

    @Autowired
    private LogApplyService logApplyService;

    @Override
    public Connection getConn(LogApplyVO logApplyVO) {
        Connection conn = new Connection(logApplyVO.getAuditIp(), Integer.parseInt(logApplyVO.getAuditPort()));
        try {
//            conn.connect();
            conn.connect(new ServerHostKeyVerifier() {
                @Override
                public boolean verifyServerHostKey(String hostname, int port,
                                                   String serverHostKeyAlgorithm, byte[] serverHostKey)
                        throws Exception {
                    return true;
                }
            },80000,0);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            boolean isValid = conn.authenticateWithPassword(logApplyVO.getAuditName(), logApplyVO.getAuditPwd());
            if (!isValid) {
                conn.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }

    @Override
    public Result verfyNamePwd(Authentication authentication) {
        Result result = new Result("","");
        Connection ssh2conn = new Connection(authentication.getAuditIp(), Integer.parseInt(authentication.getAuditPort()));
        try {
//            ssh2conn.connect();
            ssh2conn.connect(new ServerHostKeyVerifier() {
                @Override
                public boolean verifyServerHostKey(String hostname, int port,
                                                   String serverHostKeyAlgorithm, byte[] serverHostKey)
                        throws Exception {
                    return true;
                }
            },5000,0);
        } catch (IOException e1) {
            e1.printStackTrace();
            result.setCode("1");
            result.setErrmsg(e1.getMessage());
            return result;
        }
        try {
            boolean isValid = ssh2conn.authenticateWithPassword(authentication.getAuditName(), authentication.getAuditPwd());
            if (!isValid) {
                ssh2conn.close();
                result.setCode("2");
                result.setErrmsg("用户名密码校验失败！");
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setCode("1");
            result.setErrmsg(e.getMessage());
            return result;
        }
        result.setCode("0");
        result.setErrmsg("用户名密码校验成功！");
        return result;
    }

    @Override
    public List<Node> listFiles(LogApplyVO host, String remoteDir) {
        String result = null;
        Connection conn = null;
        Session sess = null;
        InputStream stdout = null;
        BufferedReader br = null;
        StringBuffer buffer = new StringBuffer();
        List<Node> nodeList = new ArrayList<>();

        LogApplyVO logApplyVO = new LogApplyVO();

        try {
            //模拟数据
            if(host==null){
                logApplyVO.setAuditIp("172.18.6.8");
                logApplyVO.setAuditPort("22");
                logApplyVO.setAuditName("root");
                logApplyVO.setAuditPwd("12345678.com");
                conn = getConn(logApplyVO);
            }else{
                conn = getConn(host);
            }

            sess = conn.openSession();
            //String cmd = "ls -l " + remoteDir;
            String cmd = "ls --file-type -m " + remoteDir;
            sess.execCommand(cmd);
            stdout = new StreamGobbler(sess.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                // attention: do not comment this block, or you will hit
                // NullPointerException
                // when you are trying to read exit status
                String line = br.readLine();
                if (line == null)
                    break;

                //处理每行，返回数组
                String[] strArr = line.split(",");

                for (String str0: strArr) {
                    String str=str0.trim();
                    Node node= new Node();

                    if(str.indexOf("/")!=-1){
                        node.setType("dir");
                        node.setName(str.substring(0,str.length()-1));
                        node.setPath(remoteDir.trim()+str);
                    }else if(str.indexOf("/")==-1 && str.indexOf("@")==-1 && str.indexOf("|")==-1 && str.indexOf("=")==-1){
                        //“/”表示目录，“@”表示符号链接，“|”表示命令管道FIFO，“=”表示sockets套接字。当文件为普通文件时，不输出任何标识符；
                        node.setType("file");
                        node.setName(str);
                        node.setPath(remoteDir.trim()+str);
                    }
                    if(node.getName()!=null){
                        nodeList.add(node);
                    }
                }
//                buffer.append(line);
//                buffer.append(System.getProperty("line.separator"));// 换行
//                if (logger.isInfoEnabled()) {
//                    logger.info(line);
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sess.close();
            conn.close();
        }
        return nodeList;
    }


    @Override
    public String execShellWithStdout(Connection conn, String shellCmd) {
        //StringBuilder result = new StringBuilder();
        String results = "";
        Session session = null;
        try {
            // 一个session只能执行一次shell
            session = conn.openSession();
            session.execCommand(shellCmd);
            InputStream stdout = new StreamGobbler(session.getStdout());
            try {
                // 阻塞等待流传输过来，限定30k
                session.waitForCondition(ChannelCondition.EOF, 30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] buffer = new byte[100];
            // 循环从控制台输出流中读取数据
            while (stdout.available() > 0) {
                int le = stdout.read(buffer);
                if (le > 0) {
                    results += new String(buffer, 0, le);
                }
            }
            //System.out.println(results);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
        return results;
    }

    @Override
    public String copyTar(LogApplyVO host, String localFile, String remoteDir,String msg) {
        //// TODO: 2017/10/9 并发拷贝文件
        logger.info("@@@@@@@@@@@拷贝开始："+msg);
        Connection connection = getConn(host);
        SCPClient scpClient = null;
        try {
            scpClient = connection.createSCPClient();
            scpClient.put(localFile, remoteDir,"0777");
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("@@@@@@@@@@@拷贝完成："+msg);
        //// TODO: 2017/10/9  根据文件大小判断是否完成传输
        return remoteDir+remoteDir;
    }

    @Override
    public Result deployFlume(LogApplyVO host) {
        Result result =  new Result("0","");
        //1、获取连接
        Connection connection= getConn(host);

        String javaVersion = execShellWithStdout(connection,"whereis java | awk -F ':' '{print $2}' | awk -F ' ' '{print $2}'");
        if(StringUtils.isEmpty(javaVersion)){
            result.setErrmsg("java环境变量未设置!");
            connection.close();
            return result;
        }

        //2、判断是否已经部署
        String isExist = execShellWithStdout(connection,"ls "+remoteFlumeDir+"/apache-flume-1.7.0-bin");
        //4、如果为空，执行上传
        if(StringUtils.isEmpty(isExist)){
            try {
               copyTar(host,localFlumeTar,remoteFlumeDir,"拷贝flume安装包到远端");
                //5、执行解压,并且修改权限
                execShellWithStdout(connection,"tar -zxvf /tmp/apache-flume-1.7.0-bin.tgz -C /tmp ; chmod 777 /tmp/apache-flume-1.7.0-bin  ");
            } catch (Exception e) {
                copyTar(host,localFlumeTar,remoteFlumeDir,"重新断点续传拷贝flume安装包到远端");
                //e.printStackTrace();
            }
        }

        //6、配置文件生成
        //6.1、/etc/host配置文件修改，加入主机列表
        //6.2、checkpointDir 属性路径设置，默认flume部署位置
        FlumeAgentConfig flumeAgentConfig = new FlumeAgentConfig();
        //获取filelist
        List<String> files = logApplyService.getFilesByIp(host.getAuditIp());
        String flumeCfgName = flumeAgentConfig.genAgentConfig(host.getAuditIp(), files, host.getKafkaServer(), host.getKafkaTopic());
        logger.info("flume agent 配置已经创新生成:"+flumeCfgName);
        //6.3 配置文件同步到远程主机
        copyTar(host,flumeCfgName,remoteFlumeDir+"/apache-flume-1.7.0-bin/conf","拷贝配置文件到远程机器");

        //7、判断是否已经启动,通过验证端口是否开启判断脚本是否成功运行
        String executeInfo=execShellWithStdout(connection, "ps -ef | grep flumeagent | grep -v grep");
        if(!StringUtils.isEmpty(executeInfo)) {
            logger.info("flume agent 已经启动："+executeInfo);
        }else{
            //9、执行启动
            String startupCmd="/bin/bash /tmp/apache-flume-1.7.0-bin/store/flumeagent_start.sh";
            logger.info("执行远程部署命令："+startupCmd);

            String runMsg = execShellWithStdout(connection,startupCmd);
            String runInfo = execShellWithStdout(connection, "ps -ef | grep flumeagent | grep -v grep");
            if(!StringUtils.isEmpty(runInfo)){
                logger.info("flume agent 启动成功："+runInfo);
            }else{
                logger.info("flume agent 启动失败,异常信息："+runMsg);
                connection.close();
                result.setErrmsg("flume agent 启动失败,异常信息："+runMsg);
                return result;
            }

        }
        connection.close();
        result.setCode("1");
        return result;
    }


}
