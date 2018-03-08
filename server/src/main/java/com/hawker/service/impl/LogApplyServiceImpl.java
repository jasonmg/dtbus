package com.hawker.service.impl;

import com.github.pagehelper.util.StringUtil;
import com.hawker.dao.LogApplyMapper;
import com.hawker.pojo.LogApply;
import com.hawker.pojo.LogApplyVO;
import com.hawker.service.LogApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogApplyServiceImpl implements LogApplyService {
    @Autowired
    private LogApplyMapper logApplyMapper;

    @Override
    public LogApply save(LogApply logApply) {
        int rescode =  logApplyMapper.applySave(logApply);
        return logApply;
    }

    @Override
    public List<String> getFilesByIp(String auditIp) {
        return logApplyMapper.getFilesByIp(auditIp);
    }

    @Override
    public List<LogApplyVO> findApplyByFilter(String auditIp, String fileName,String userId, String sorts) {
        //拼接where条件，默认取有效记录
        StringBuffer strfilter = new StringBuffer(" 1=1 ");
        if(!StringUtil.isEmpty(auditIp)){
            strfilter.append(" and audit_ip='");
            strfilter.append(auditIp);
            strfilter.append("'");
        }

        if(!StringUtil.isEmpty(fileName)){
            strfilter.append(" and file_name='");
            strfilter.append(fileName);
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

        List<LogApplyVO> list = logApplyMapper.selectApplyByFilter(strfilter.toString());

        return  list;
    }

    @Override
    public int deleteLogApplyById(int id) {
        return logApplyMapper.deleteLogApplyById(id);
    }
}
