package com.hawker.dao;

import com.hawker.pojo.LogApply;
import com.hawker.pojo.LogApplyVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LogApplyMapper {

    //保存鉴权信息，返回增加的Id
    @Insert("insert into log_apply (audit_id,file_name,is_full,is_valid,kafka_server,kafka_topic) values (#{auditId},#{fileName},#{isFull},#{isVaild},#{kafkaServer},#{kafkaTopic})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int applySave(LogApply logApply);


    @Select("select ap.id,ad.id as audit_id,ad.audit_ip,ad.audit_port,ad.audit_name,ad.audit_pwd,\n" +
            "ap.file_name,ap.remark,ap.is_full,ap.kafka_server,ap.kafka_topic\n" +
            "from dt_audit ad\n" +
            "left join log_apply ap\n" +
            "on ap.audit_id=ad.id " +
            "where ad.type='os' and ${strfilter}")
    List<LogApplyVO> selectApplyByFilter(@Param("strfilter") String strfilter);

    @Select("SELECT distinct file_name\n" +
            "FROM dt_audit a\n" +
            "inner join log_apply b  on a.id= b.audit_id\n" +
            "where a.audit_ip=#{auditIp}")
    List<String> getFilesByIp(@Param("auditIp") String auditIp);


    @Delete("delete from log_apply where id = ${id}")
    int deleteLogApplyById(@Param("id") int id);
}
