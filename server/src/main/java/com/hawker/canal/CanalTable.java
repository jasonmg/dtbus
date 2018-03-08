package com.hawker.canal;

import fj.Ord;
import fj.Ordering;

/**
 * @author mingjiang.ji on 2017/9/17
 */
public class CanalTable {

    // table id
    private int id;

    private String ip;
    private String port;
    private String usr;
    private String psword;

    private String dataBase;
    private String table;

    // 是否拉取过全量
    private boolean fullPulled;

    // 是否全量
    private boolean isFullPull;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getPsword() {
        return psword;
    }

    public void setPsword(String psword) {
        this.psword = psword;
    }

    public String getDataBase() {
        return dataBase;
    }

    public void setDataBase(String dataBase) {
        this.dataBase = dataBase;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


    public String getFilter() {
        return dataBase + "." + table;
    }

    public String getIP_DB() {
        return ip + "_" + dataBase;
    }

    public boolean isFullPulled() {
        return fullPulled;
    }

    public void setFullPulled(boolean fullPulled) {
        this.fullPulled = fullPulled;
    }

    public boolean isFullPull() {
        return isFullPull;
    }

    public void setFullPull(boolean fullPull) {
        isFullPull = fullPull;
    }

    /**
     * 是否应该拉取全量, 判断方法
     * 1: 有全量标志       isFullPull = true
     * 2: 还没有拉取过全量  fullPulled = false
     */
    public boolean shouldFullPull() {
        return isFullPull && !fullPulled;
    }

    /**
     * 返回不重复的canal client instance
     */
    public String nonDuplicateStr() {
        return ip + "_" + dataBase + "_" + table;
    }

    public static Ord<CanalTable> canalTableOrd = Ord.ord((CanalTable a, CanalTable b) -> {
        if (a.nonDuplicateStr().equals(b.nonDuplicateStr()))
            return Ordering.EQ;
        else
            return Ordering.LT;
    });
}
