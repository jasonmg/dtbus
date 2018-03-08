package com.hawker.dao;

import com.hawker.pojo.*;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface TotalMapper {
    //保存鉴权信息，返回增加的Id
    @Insert("insert into dt_audit (audit_ip,audit_port,audit_name,audit_pwd,is_valid,type,user_id) values (#{auditIp},#{auditPort},#{auditName},#{auditPwd},#{isVaild},#{type},#{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int auditSave(Authentication audit);

    //保存数据库信息
    @Insert("insert into dt_db (db_name,cs_name,ct_name) values (#{dbName},#{csName},#{ctName})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int dtDbSave(DataBase db);

    /***
     * 鉴权用户和库对应关系保存
     * @param auditId 鉴权用户Id
     * @param dbID 数据库ID
     * @param isValid 是否有效，默认1：有效
     * @param begdate 有效开始时间
     */
    @Insert("insert into dt_audit_db (audit_id,db_id,is_vaild,begdate) values (#{auditId},#{dbID},#{isValid},#{begdate})")
    void dtAuditDbSave(@Param("auditId")int auditId,@Param("dbID")int dbID,@Param("isValid") int isValid ,@Param("begdate")Date begdate);


    //表信息保存
    @Insert("insert into dt_table " +
            "(db_id,table_name,engine,table_rows,auto_increment,create_time,table_collation,table_comment,is_vaild) values" +
            " (#{dbId},#{tableName},#{engine},#{tableRows},#{autoIncrement},#{createTime},#{tableCollation},#{tableComment},#{isValid})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int dtTableSave(Table table);


    /***
     *  保存字段信息
     * @param db_id 数据库id
     * @param table_id 表Id
     * @param column_name 字段名称
     * @param data_type 数据类型
     * @param character_maximum_length  最大长度
     * @param column_comment 备注
     */
    @Insert("insert into dt_field (db_id,table_id,column_name,data_type,character_maximum_length," +
            "column_comment,is_vaild) values (#{db_id},#{table_id},#{column_name},#{data_type}," +
            "#{character_maximum_length}," +
            "#{column_comment},'1')")
    void dtFieldSave(@Param("db_id")int db_id,@Param("table_id")int table_id,
                       @Param("column_name")String column_name,@Param("data_type")String data_type,
                       @Param("character_maximum_length")long character_maximum_length,
                       @Param("column_comment")String column_comment);



    @Insert("insert ignore into dt_audit (audit_ip,audit_port,audit_name,audit_pwd,ctime) values (#{address},#{port},#{username},#{password},#{ctime})")
    void insertAu(@Param("username")String username,@Param("password")String password,@Param("address")String address,@Param("port")String port,@Param("ctime")Date ctime);


    @Insert("insert ignore into dt_audit_db (audit_name,db_name,is_vaild,begdate) values (#{username},#{dbname},'1',#{begdate})")
    void insertdtAuditDb(@Param("username")String username,@Param("dbname")String dbname,@Param("begdate")Date begdate);

    @Insert("insert ignore into dt_db (db_name,cs_name,ct_name) values (#{dbname},#{csname},#{ctname})")
    void insertDtDb(@Param("dbname")String dbname,@Param("csname")String csname,@Param("ctname")String ctname);

    @Insert("insert ignore into dt_table (db_name,table_name,engine,table_rows,auto_increment,create_time," +
            "table_collation,table_comment,is_vaild) values" +
            " (#{db_name},#{table_name},#{engine},#{table_rows},#{auto_increment},#{create_time},#{table_collation" +
            "},#{table_comment},'1')")
    void insertDtTable(@Param("db_name")String db_name,@Param("table_name")String table_name,
                       @Param("engine")String engine,@Param("table_rows")String table_rows,
                       @Param("auto_increment")String auto_increment,@Param("create_time")Date create_time,
                       @Param("table_collation")String table_collation,@Param("table_comment")String table_comment);


    @Insert("insert ignore into dt_field (db_name,table_name,column_name,data_type,character_maximum_length," +
            "column_comment,is_vaild) values (#{db_name},#{table_name},#{column_name},#{data_type}," +
            "#{character_maximum_length}," +
            "#{column_comment},'1')")
    void insertDtField(@Param("db_name")String db_name,@Param("table_name")String table_name,
                       @Param("column_name")String column_name,@Param("data_type")String data_type,
                       @Param("character_maximum_length")String character_maximum_length,
                       @Param("column_comment")String column_comment);


    @Select("select * from dt_db where db_name in (select db_name from dt_audit_db where " +
            "audit_name=#{username} and db_name=#{dbname})")
    List<DataBase> selectDb(@Param("username")String username, @Param("dbname")String dbname);

    @Select("select * from dt_audit")
    List<Authentication> selectAuthentication();

    @Select("select * from dt_table where db_name =#{db_name}")
    List<Table> selectTables(@Param("db_name")String db_name);

    @Select("select * from dt_field where db_name =#{db_name} and table_name=#{table_name}")
    List<Field> selectFields(@Param("table_name")String table_name, @Param("db_name")String db_name);



    @Select("SELECT *FROM dt_audit WHERE id IN (SELECT dad.audit_id FROM dt_audit_db dad WHERE dad.db_id IN (SELECT dt.db_id FROM dt_table dt WHERE dt.id = #{id}))")
    List<Authentication> selectAu(@Param("id")String id);

    @Select("select t.table_name,d.db_name  from dt_table t LEFT JOIN dt_db d on t.db_id = d.id  where t.id = #{id}")
    List<Filter> selectFilter(@Param("id")String id);



    @Select("select * from dt_canal_mq where ip =#{ip} and port=#{port} ")
    List<Apply> selectApplie(@Param("ip")String ip, @Param("port")String port);



    @Insert("insert  into dt_canal_mq (ip,port,filter,updatetime)" +
            " values (#{ip},#{port},"+
            "#{filter},#{updatetime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertdtCanalMq(Apply apply);

    @Update("update dt_canal_mq set filter =#{filter},updatetime=#{updatetime} where ip=#{ip} and port=#{port}")
    void updateApply(@Param("filter")String filter,
                            @Param("ip")String ip,@Param("port")String port,@Param("updatetime")Date updatetime);


    @Select("select id from dt_table where db_id =#{id}")
    List<String> selectTableid(@Param("id")String id);


    @Select("select * from dt_canal_mq")
    List<Apply> selectApplies();

    @Delete("delete from  dt_canal_mq where id =#{id}")
    void deleteById(@Param("id") String id);

    @Delete("select * from  dt_canal_mq where id =#{id}")
    List<Apply> selectById(@Param("id") String id);


    @Select("select * from dt_audit where is_valid =1 and type='mysql'")
    List<Authentication> selectAuditList();

    @Select("select c.id,c.db_name from dt_audit a inner join dt_audit_db b on a.id= b.audit_id inner join dt_db c on b.db_id = c.id where is_valid =1 and a.id=#{auditId}")
    List<DataBase> selectDataBaseListByAuditId(@Param("auditId") Integer auditId);

    @Select("select d.id,d.table_name,d.table_comment from dt_audit a inner join dt_audit_db b on a.id= b.audit_id inner join dt_db c on b.db_id = c.id inner join dt_table d on c.id=d.db_id where  is_valid =1 and c.id=#{dbId}")
    List<Table> selectTableListByDbId(@Param("dbId") Integer dbId);
}
