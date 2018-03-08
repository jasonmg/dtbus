package com.hawker.service;

import com.github.pagehelper.Page;
import com.hawker.pojo.Authentication;
import com.hawker.pojo.Result;

import java.util.List;


public interface AuditService {
    /***
     * 分页和条件查询
     * @param auditIp  主机地址
     * @param auditName 鉴权名称
     * @param sorts   排序
     * @param pageNo   页号
     * @param pageSize 每页显示记录数
     * @return
     */
    public List<Authentication> findAuditByFilter(String auditIp, String auditName, String type,String userId, String sorts, int pageNo, int pageSize);

    //根据ID查找对象
    public Authentication findAuditById(Integer id);

    //patch鉴权信息修改
    public Authentication auditUpdate(Authentication audit);

    //update鉴权信息修改
    public Authentication auditPatch(Authentication audit);


    //根据ID删除对象
    public int deleteAuditById(String id);
}
