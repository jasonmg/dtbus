package com.hawker.dao;

import com.hawker.pojo.Authentication;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuditMapper {

    @Select("select * from dt_audit where ${strfilter}")
    List<Authentication> selectAuditByFilter(@Param("strfilter") String strfilter);

    @Select("select * from dt_audit where id=#{id}")
    Authentication findAuditById(@Param("id") Integer id);

    @Update("update dt_audit set audit_ip=#{auditIp},audit_port=#{auditPort},audit_name=#{auditName},audit_pwd=#{auditPwd},is_valid=#{isVaild} where id=#{id}")
    int auditUpdate(Authentication audit);

    @Update("update dt_audit set ${strpatch}")
    int auditPatch(@Param("strpatch") String strpatch);

    //使用逻辑删除更新删除标志，而非使用注解@Delete("delete from dt_audit where id=#{id}")
//    @Update("update dt_audit set is_valid=0 where id=${id}")
//    public int deleteAuditById(@Param("id") int id);

    //使用逻辑删除更新删除标志，而非使用注解@Delete("delete from dt_audit where id=#{id}")
    @Delete("delete from dt_audit where id=${id}")
    public int deleteAuditById(@Param("id") int id);

}
