package com.hawker.service.impl;

import com.hawker.dao.TotalMapper;
import com.hawker.pojo.*;
import com.hawker.service.ListMysqlService;
import com.hawker.service.SSH2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ListMysqlServiceImpl implements ListMysqlService {
    @Autowired
    private TotalMapper ma;

    @Autowired
    private SSH2Service ssh2Service;

    /***
     *保存鉴权到数据库
     * @param authentication
     * @return
     */
    public Result authOS(Authentication authentication) {
        //调用鉴权，进行操作系统用户密码验证，成功后保存数据库
        Result result = ssh2Service.verfyNamePwd(authentication);
        if ("0".equals(result.getCode())) {
            int ret = ma.auditSave(authentication);
            return result;
        } else {
            return result;
        }

    }

    /***
     * 鉴权（入库，获取所有的数据库信息）
     * @param username 鉴权用户名称
     * @param password 鉴权密码
     * @param ip 主机Ip
     * @param port 主机端口
     * @param type 类型mysql
     * @return 对象
     */
    public Result authentication(String username, String password, String ip, String port, String type, String userId) {
        //连接鉴权库，获取信息
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        String address = "jdbc:mysql://" + ip + ":" + port;
        dataSourceBuilder.driverClassName("com.mysql.jdbc.Driver");
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        dataSourceBuilder.url(address);
        DataSource dataSource = dataSourceBuilder.build();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result("1", "添加鉴权失败,连接信息有误！" + e.getMessage());
        }

        //鉴权信息保存，返回id
        //ma.insertAu(username, password, ip, port,new Date());
        Authentication audit = new Authentication();
        audit.setAuditIp(ip);
        audit.setAuditPort(port);
        audit.setAuditName(username);
        audit.setAuditPwd(password);
        audit.setType(type);
        audit.setIsVaild(1);
        audit.setUserId(userId);

        int id1 = ma.auditSave(audit);
        int auditId = audit.getId();

        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("select * from information_schema.SCHEMATA where SCHEMA_NAME not in ('information_schema','performance_schema','mysql','sys');");
            rs = ps.executeQuery();
            while (rs.next()) {
                //保存当前数据库信息到库表
                //ma.insertDtDb(rs.getString(2),rs.getString(3),rs.getString(4));
                DataBase db = new DataBase();
                String dbname = rs.getString(2);
                db.setDbName(dbname);
                db.setCsName(rs.getString(3));
                db.setCtName(rs.getString(4));
                int id2 = ma.dtDbSave(db);
                int dbId = db.getId();

                //插入鉴权和库对应关系
                //ma.insertdtAuditDb(username,rs.getString(2),new Date());
                ma.dtAuditDbSave(auditId, dbId, 1, new Date());

                //根据库获取表信息
                String sql1 = "select * from information_schema.tables where table_schema=?;";
                try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                    ps1.setString(1, rs.getString(2));
                    try (ResultSet rs1 = ps1.executeQuery()) {
                        while (rs1.next()) {
                            //插入表信息
                            Table table = new Table();
                            table.setDbId(dbId);
                            table.setTableName(rs1.getString(3));
                            table.setEngine(rs1.getString(5));
                            table.setTableRows(rs1.getString(8));
                            table.setAutoIncrement(rs1.getString(14));
                            Timestamp ts = Timestamp.valueOf(rs1.getString(15));
                            table.setCreateTime(ts);
                            table.setTableCollation(rs1.getString(18));
                            table.setTableComment(rs1.getString(21));
                            table.setIsValid(1);
                            int id3 = ma.dtTableSave(table);
                            int tableId = table.getId();


                            //根据表获取列信息
                            String sql2 = "select * from information_schema.columns where table_name=? and table_schema=?";
                            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                                ps2.setString(1, rs1.getString(3));
                                ps2.setString(2, rs1.getString(2));
                                try (ResultSet rs2 = ps2.executeQuery()) {
                                    while (rs2.next()) {
                                        //保存列信息
                                        ma.dtFieldSave(dbId, tableId, rs2.getString(4),
                                                rs2.getString(8), rs2.getLong(9), rs2.getString(20));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return new Result("0", "ok!");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result("2", "sql查询或插入语句异常");
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /* ignored */}
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) { /* ignored */}
            }

            if (conn != null) try {
                conn.close();
            } catch (SQLException e) { /* ignored */}
        }
    }

    @Override
    public Result getDb(String username, String dbname) {
        List<DataBase> list = ma.selectDb(username, dbname);
        if (list.size() == 0) {
            return new Result("1", "数据库无此信息");
        }
        Result result = new Result("0", "");
        result.setData(list);
        result.setRows(list.size());
        return result;
    }

    @Override
    public Result getAllAu() {
        List<Authentication> list = ma.selectAuthentication();
        if (list.size() == 0) {
            return new Result("1", "数据库无此信息");
        }
        Result result = new Result("0", "");
        result.setData(list);
        result.setRows(list.size());
        return result;
    }

    @Override
    public Result getTable(String dbname) {
        List<Table> list = ma.selectTables(dbname);
        if (list.size() == 0) {
            return new Result("1", "数据库无此信息");
        }
        Result result = new Result("0", "");
        result.setData(list);
        result.setRows(list.size());
        return result;
    }

    @Override
    public Result getField(String tableName, String dbName) {
        List<Field> list = ma.selectFields(tableName, dbName);
        if (list.size() == 0) {
            return new Result("1", "数据库无此信息");
        }
        Result result = new Result("0", "");
        result.setData(list);
        result.setRows(list.size());
        return result;
    }

}
