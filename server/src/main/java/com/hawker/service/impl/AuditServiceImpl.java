package com.hawker.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.hawker.canal.binlog.dump.PullManager;
import com.hawker.dao.ApplyMapper;
import com.hawker.dao.AuditMapper;
import com.hawker.dao.TotalMapper;
import com.hawker.pojo.Authentication;
import com.hawker.pojo.Result;
import com.hawker.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditServiceImpl  implements AuditService{

    @Autowired
    private AuditMapper auditMapper;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    PullManager pullManager;

    @Override
    public List<Authentication> findAuditByFilter(String auditIp, String auditName, String type,String userId, String sorts, int pageNo, int pageSize) {

        //拼接where条件，默认取有效记录
        StringBuffer strfilter = new StringBuffer(" is_valid=1 ");

        if(!StringUtil.isEmpty(auditIp)){
            strfilter.append(" and audit_ip='");
            strfilter.append(auditIp);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(auditName)){
            strfilter.append(" and audit_name='");
            strfilter.append(auditName);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(type)){
            strfilter.append(" and type='");
            strfilter.append(type);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(userId)){
            strfilter.append(" and user_id='");
            strfilter.append(userId);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(sorts)){
            strfilter.append(" order by "+sorts);
        }

        List<Authentication> list = auditMapper.selectAuditByFilter(strfilter.toString());

        return  list;

    }

    /***
     * Patch修改鉴权信息（暂时重新刷用户数据，后期调用刷新数据处理）
     * @param audit
     * @return
     */
    @Override
    public Authentication auditUpdate(Authentication audit) {

        int updateid = auditMapper.auditUpdate(audit);
        // TODO 后期将用户数据重刷
        return audit;
    }


    /***
     * Patch修改鉴权信息（暂时重新刷用户数据，后期调用刷新数据处理）
     * @param audit 要更新的对象
     * @return 更新后的对象
     */
    @Override
    public Authentication auditPatch(Authentication audit) {
        //拼接where条件，默认取有效记录
        StringBuffer strpatch = new StringBuffer(" id=");
        strpatch.append(audit.getId());

        if(!StringUtil.isEmpty(audit.getAuditPort())){
            strpatch.append(" , audit_port='");
            strpatch.append(audit.getAuditPort());
            strpatch.append("'");
        }

        if(!StringUtil.isEmpty(audit.getAuditPwd())){
            strpatch.append(" , audit_pwd='");
            strpatch.append(audit.getAuditPwd());
            strpatch.append("'");
        }

        strpatch.append(" where id=");
        strpatch.append(audit.getId());
        auditMapper.auditPatch(strpatch.toString());

        // TODO 后期将用户数据重刷

        return findAuditById(audit.getId());
    }

    @Override
    @Transactional
    public int deleteAuditById(String id) {

        //逻辑删除鉴权信息
        auditMapper.deleteAuditById(Integer.parseInt(id));
        //删除该鉴权的所有申请
        applyMapper.deleteApplyByAuditId(Integer.parseInt(id));
        //调用canal client删除同步
//        pullManager.removeCanalInstance(Integer.parseInt(id));

        return 1;
    }

    @Override
    public Authentication findAuditById(Integer id) {
        return auditMapper.findAuditById(id);
    }


}
