package com.hawker.pojo;

import java.sql.Timestamp;

/***
 * 虚拟实体类，包装申请信息返回类
 */
public class LogApplyVO {
    private int id;
    private Integer auditId;
    private String auditIp;
    private String auditPort;
    private String auditName;
    private String auditPwd;
    private String fileName;
    private String remark;
    private Boolean fullPulled;
    private Integer isFull;
    private Integer isValid;
    private String kafkaServer;
    private String kafkaTopic;
    private Timestamp optime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getAuditId() {
        return auditId;
    }

    public void setAuditId(Integer auditId) {
        this.auditId = auditId;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIsFull() {
        return isFull;
    }

    public void setIsFull(Integer isFull) {
        this.isFull = isFull;
    }

    public String getKafkaServer() {
        return kafkaServer;
    }

    public void setKafkaServer(String kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public Timestamp getOptime() {
        return optime;
    }

    public void setOptime(Timestamp optime) {
        this.optime = optime;
    }

    public Boolean getFullPulled() {
        return fullPulled;
    }

    public void setFullPulled(Boolean fullPulled) {
        this.fullPulled = fullPulled;
    }
}
