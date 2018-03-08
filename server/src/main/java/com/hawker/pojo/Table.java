package com.hawker.pojo;

import java.sql.Timestamp;

/**
 * Created by 金皓天 on 2017/7/13.
 */
public class Table {
    private Integer id;
    private Integer dbId;
    private String tableName;
    private String engine;
    private String tableRows;
    private String autoIncrement;
    private Timestamp createTime;
    private String tableCollation;
    private String tableComment;
    private Integer isValid;
    private Integer fullPulled;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getTableRows() {
        return tableRows;
    }

    public void setTableRows(String tableRows) {
        this.tableRows = tableRows;
    }

    public String getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(String autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getTableCollation() {
        return tableCollation;
    }

    public void setTableCollation(String tableCollation) {
        this.tableCollation = tableCollation;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public Integer getFullPulled() {
        return fullPulled;
    }

    public void setFullPulled(Integer fullPulled) {
        this.fullPulled = fullPulled;
    }
}
