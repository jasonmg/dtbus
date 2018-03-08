package com.hawker.pojo;

import java.sql.Timestamp;

public class LogApply {
    private int id;
    private Integer auditId;
    private String fileName;
    private Integer isFull;
    private Integer fullPulled;
    private Integer isVaild;
    private String kafkaServer;
    private String kafkaTopic;
    private String remark;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public Integer getFullPulled() {
        return fullPulled;
    }

    public void setFullPulled(Integer fullPulled) {
        this.fullPulled = fullPulled;
    }

    public Integer getIsVaild() {
        return isVaild;
    }

    public void setIsVaild(Integer isVaild) {
        this.isVaild = isVaild;
    }

    public Timestamp getOptime() {
        return optime;
    }

    public void setOptime(Timestamp optime) {
        this.optime = optime;
    }

    public Integer getIsFull() {
        return isFull;
    }

    public void setIsFull(Integer isFull) {
        this.isFull = isFull;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
