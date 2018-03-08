package com.hawker.dao;

import com.hawker.pojo.Apply;
import com.hawker.pojo.ApplyVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApplyMapper {

    //保存鉴权信息，返回增加的Id
    @Insert("insert into dt_apply (audit_id,db_id,table_id,is_full,is_valid,kafka_server,kafka_topic) values (#{auditId},#{dbId},#{tableId},#{isFull},#{isVaild},#{kafkaServer},#{kafkaTopic})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int applySave(Apply apply);

    @Select("select ap.id,ap.audit_id,ap.db_id,ap.table_id,tb.full_pulled,audit_ip,audit_port,audit_name,audit_pwd,db.db_name," +
            "case when ap.table_id=0 then '*' else tb.table_name end as table_name,tb.table_comment,ap.is_full," +
            "ap.kafka_server, concat_ws('_',audit_ip,audit_port,db.db_name,tb.table_name) as kafka_topic\n" +
            "from dt_apply ap\n" +
            "inner join dt_audit ad\n" +
            "on ap.audit_id=ad.id  and ad.is_valid=1\n" +
            "inner join dt_db db\n" +
            "on ap.db_id = db.id\n" +
            "left join dt_table tb\n" +
            "on ap.table_id=tb.id  where ${strfilter} order by ap.id desc")
    List<ApplyVO> selectApplyByFilter(@Param("strfilter") String strfilter);

    @Select("select CONCAT_WS('&',audit_id,str) as str\n" +
            "from (\n" +
            "\tselect audit_id,GROUP_CONCAT(CONCAT_WS('#',is_full,dtb_name) SEPARATOR '@') as str\n" +
            "\tfrom (\n" +
            "\t\tSELECT ap.is_full,ap.audit_id,\n" +
            "\t\t GROUP_CONCAT(CONCAT_WS('.',db.db_name,case when ap.table_id=0 then '*' else tb.table_name end)) as dtb_name\n" +
            "\t\tFROM dt_apply ap \n" +
            "\t\tINNER JOIN dt_audit ad ON ap.audit_id = ad.id AND ad.type='mysql' -- AND ad.is_valid = 1 \n" +
            "\t\tINNER JOIN dt_db db ON ap.db_id = db.id \n" +
            "\t\tleft JOIN dt_table tb ON ap.table_id = tb.id \n" +
            "\t\tgroup by ap.is_full,ap.audit_id\n" +
            "\t) a\n" +
            "\tgroup by a.audit_id\n" +
            ") b")
    List<String> selectAllApply();

    @Delete("delete from dt_apply where id = ${id}")
    int deleteApplyById(@Param("id") int id);


    @Delete("delete from dt_apply where audit_id = ${auditId}")
    int deleteApplyByAuditId(@Param("auditId") int auditId);

    @Update("update dt_table set full_pulled=1 where id=#{id}")
    int setToFullPulled(@Param("id") int id);
}
