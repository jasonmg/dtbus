package com.hawker.pojo;

import java.sql.Timestamp;

public class Authentication {
    private Integer id;
    private String auditIp;
    private String auditPort;
    private String auditName;
    private String auditPwd;
    private Timestamp ctime;
    private Integer isVaild;
    private String type;  // mysql 为 mysql鉴权  os 为操作系统鉴权
    private String remark;  //备注
    private String userId;//dtbus登录用户Id


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuditIp() {
        return auditIp;
    }

    public void setAuditIp(String auditIp) {
        this.auditIp = auditIp;
    }

    public String getAuditPort() {
        return auditPort;
    }

    public void setAuditPort(String auditPort) {
        this.auditPort = auditPort;
    }

    public String getAuditName() {
        return auditName;
    }

    public void setAuditName(String auditName) {
        this.auditName = auditName;
    }

    public String getAuditPwd() {
        return auditPwd;
    }

    public void setAuditPwd(String auditPwd) {
        this.auditPwd = auditPwd;
    }

    public Timestamp getCtime() {
        return ctime;
    }

    public void setCtime(Timestamp ctime) {
        this.ctime = ctime;
    }

    public Integer getIsVaild() {
        return isVaild;
    }

    public void setIsVaild(Integer isVaild) {
        this.isVaild = isVaild;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
