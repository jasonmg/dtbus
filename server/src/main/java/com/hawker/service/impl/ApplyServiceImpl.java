package com.hawker.service.impl;

import com.github.pagehelper.util.StringUtil;
import com.hawker.canal.CanalInstance;
//import com.hawker.canal.binlog.dump.PullManager;
import com.hawker.canal.CanalTable;
import com.hawker.canal.binlog.dump.PullManager;
import com.hawker.config.KafkaProducerConfig;
import com.hawker.dao.ApplyMapper;
import com.hawker.dao.TotalMapper;
import com.hawker.pojo.*;
import com.hawker.service.ApplyService;
import com.hawker.service.AuditService;
import com.hawker.service.KafkaProducerService;
import com.hawker.service.KafkaTopicManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import scala.Int;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by 金皓天 on 2017/7/17.
 */
@Service
public class ApplyServiceImpl implements ApplyService {

    @Autowired
    KafkaProducerService kafkaProducerService;

    @Autowired
    PullManager pullManager;

    @Autowired
    private TotalMapper ma;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    private AuditService auditService;

    @Override
    public List<TreeNode> auditTreeNodes() {
        //获取鉴权连接列表
        List<Authentication>  auditList= ma.selectAuditList();
        List<TreeNode> auditTreeList = new ArrayList<>();
        TreeNode auditTreeNode;
        for(Authentication audit : auditList){
            //拼接鉴权连接
            auditTreeNode =  new TreeNode();
            auditTreeNode.setValue(audit.getId().toString());
            String label= audit.getAuditIp()+":"+audit.getAuditPort()+"-"+audit.getAuditName();
            auditTreeNode.setLabel(label);

            //获取库列表
            List<DataBase>  dbList= ma.selectDataBaseListByAuditId(audit.getId());
            List<TreeNode> dbTreeList = new ArrayList<>();
            TreeNode dbTreeNode;
            for(DataBase db : dbList){
                dbTreeNode = new TreeNode();
                dbTreeNode.setValue(db.getId().toString());
                dbTreeNode.setLabel(db.getDbName());

                //获取表列表
                List<Table>  tbList= ma.selectTableListByDbId(db.getId());
                List<TreeNode> tbTreeList = new ArrayList<>();
                TreeNode tbTreeNode;
                for(Table tb : tbList){
                    tbTreeNode = new TreeNode();
                    tbTreeNode.setValue(tb.getId().toString());
                    tbTreeNode.setLabel(tb.getTableName()+"("+tb.getTableComment()+")");
                    tbTreeList.add(tbTreeNode);
                }
                dbTreeNode.setChildren(tbTreeList);
                dbTreeList.add(dbTreeNode);
            }

            //鉴权节点设置子节点
            auditTreeNode.setChildren(dbTreeList);
            //树添加鉴权节点
            auditTreeList.add(auditTreeNode);
        }

        return auditTreeList;
    }

    @Override
    public Apply save(Apply apply) {
        int rescode =  applyMapper.applySave(apply);
        return apply;
    }

    @Override
    public List<ApplyVO> findApplyByFilter(String auditIp, String dbName, String tableName, Integer tableId,String userId, String sorts) {
        //拼接where条件，默认取有效记录
        StringBuffer strfilter = new StringBuffer(" 1=1 ");
        if(!StringUtil.isEmpty(auditIp)){
            strfilter.append(" and audit_ip='");
            strfilter.append(auditIp);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(dbName)){
            strfilter.append(" and db_name='");
            strfilter.append(dbName);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(tableName)){
            strfilter.append(" and table_name='");
            strfilter.append(tableName);
            strfilter.append("'");
        }

        if(tableId>0){
            strfilter.append(" and table_id=");
            strfilter.append(tableId);
        }

        if(!StringUtil.isEmpty(userId)){
            strfilter.append(" and user_id='");
            strfilter.append(userId);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(sorts)){
            strfilter.append(" order by "+sorts);
        }

        List<ApplyVO> list = applyMapper.selectApplyByFilter(strfilter.toString());

        return  list;
    }

    @Override
    public int applySync(String ids) {

        List<String> list = applyMapper.selectAllApply();

        CanalInstance instance;
        //遍历所有有效记录，分为全量和增量数据
        // 返回数据格式为：2&0#test.dt_audit_db,test.dt_field,test.immsg@1#jctest.dt_audit
        //其中&分割auditid和过滤字符串  @分割全量和增量
        for(String s:list){
            instance=new CanalInstance();
            int auditId = Integer.parseInt(s.split("&")[0]);

            instance.setId(auditId);
            Authentication audit =  auditService.findAuditById(auditId);
            instance.setIp(audit.getAuditIp());
            instance.setPort(audit.getAuditPort());
            instance.setUsr(audit.getAuditName());
            instance.setPsword(audit.getAuditPwd());

            String filterFull="";
            String filterIncr="";

            //拆解增量，全量过滤条件
            String[] filterAra=s.split("&")[1].split("@");
            String filterStr=filterAra[0];

            //只有增量或者只有全量，则没有@拼接
            if(filterAra.length==1){
                if("1".equals(filterStr.split("#")[0])){
                    //如果只有全量
                    filterFull =filterAra[0].split("#")[1];
                    filterIncr =filterFull;
                }
                if("0".equals(filterStr.split("#")[0])){
                    //如果只有增量
                    filterIncr =filterAra[0].split("#")[1];
                }
            }else{
                if(filterStr.split("#")[0]=="1"){
                    filterFull =filterAra[0].split("#")[1];
                    filterIncr =filterAra[1].split("#")[1]+","+filterFull;
                }else {
                    filterFull =filterAra[1].split("#")[1];
                    filterIncr =filterAra[0].split("#")[1]+","+filterFull;
                }
            }

            //设置全量
//            instance.setFullFilter(filterFull);

            //设置多个增量
//            instance.setFilter(filterIncr);
            //pullManager.addCanalInstance(instance);
        }
        return 1;
    }

    @Override
    public int applyInstance(Apply apply) {
        CanalTable canalTable=new CanalTable();

        //调用查询方法获取相关数据，推送到canal接口
        List<ApplyVO> list = findApplyByFilter(null,null,null,apply.getTableId(),null,null);

        if(list.size() > 0){
            ApplyVO applyVO=list.get(0);
            canalTable.setId(applyVO.getTableId());
            canalTable.setIp(applyVO.getAuditIp());
            canalTable.setPort(applyVO.getAuditPort());
            canalTable.setUsr(applyVO.getAuditName());
            canalTable.setPsword(applyVO.getAuditPwd());
            canalTable.setDataBase(applyVO.getDbName());
            canalTable.setTable(applyVO.getTableName());
            canalTable.setFullPull(applyVO.getFullPulled());
            canalTable.setFullPull((applyVO.getIsFull()==1)?true:false);
        }
        pullManager.addCanalTable(canalTable);
        return  1;
    }


    @Override
    public List<CanalTable> applyInstanceList() {

        //调用查询方法获取相关数据，推送到canal接口
        List<ApplyVO> list = findApplyByFilter(null,null,null,0,null,null);
        List<CanalTable> canalTableList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            CanalTable canalTable=new CanalTable();
            ApplyVO applyVO=list.get(i);
            canalTable.setId(applyVO.getTableId());
            canalTable.setIp(applyVO.getAuditIp());
            canalTable.setPort(applyVO.getAuditPort());
            canalTable.setUsr(applyVO.getAuditName());
            canalTable.setPsword(applyVO.getAuditPwd());
            canalTable.setDataBase(applyVO.getDbName());
            canalTable.setTable(applyVO.getTableName());
            canalTable.setFullPulled((applyVO.getFullPulled()==null)?false:true);
            canalTable.setFullPull((applyVO.getIsFull()==0)?false:true);
            canalTableList.add(canalTable);
        }
        return canalTableList;
    }

    @Override
    public int setToFullPulled(String auditIp, String dbName, String tableName) {

        //根据入参数查询方法获取tableid，更新为增量状态
        List<ApplyVO> list = findApplyByFilter(auditIp,dbName,tableName,0,null,null);
        if(list.size() == 1) {
            ApplyVO applyVO = list.get(0);
            applyMapper.setToFullPulled(applyVO.getTableId());
        }else{
            return 0;
        }

        return 1;
    }

    @Override
    public int deleteApplyById(int id) {

        //删除数据库配置
        int retcode = applyMapper.deleteApplyById(id);
//        //判断数据库是否还有其它用户同步该表数据，如果没有则调用canal client配置端口
//        List<ApplyVO> list = findApplyByFilter(null,null,id,null,1,10);
//        if(list.size() == 0){
//            pullManager.removeCanalTable();
//        }
        return retcode;
    }

    @Override
    public int deleteApplyByAuditId(int auditId) {
        //删除数据库配置
        int retcode = applyMapper.deleteApplyByAuditId(auditId);
        return retcode;
    }

}
