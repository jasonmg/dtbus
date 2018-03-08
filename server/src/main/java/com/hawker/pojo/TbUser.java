package com.hawker.pojo;

import java.sql.Timestamp;

public class TbUser {
    private String id;
    private String userId;
    private String userName;
    private String userPwd;
    private Integer isValid;
    private Timestamp opdate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public Timestamp getOpdate() {
        return opdate;
    }

    public void setOpdate(Timestamp opdate) {
        this.opdate = opdate;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
}
