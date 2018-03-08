package com.hawker.canal;

import com.alibaba.otter.canal.client.CanalConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个 CanalInstance 对应一个数据库中 多个库的多张表
 */
public class CanalInstance {

    private int id;

    private String ip; //全量需要
    private String port;
    private String usr;
    private String psword;

    // filter list i.e.: db1.tb1,db1.tb2,db2.tb1   一个数据库ip一个list
    private List<String> filterList = new ArrayList<>();

    // full dump table list, i.e.: db1.tb1,db1.tb2 一个数据库实例一个list
    private List<String> fullDumpList = new ArrayList<>();

    private transient CanalConnector canalConn;


    public static CanalInstance from(CanalTable t) {
        CanalInstance c = new CanalInstance();
        c.setIp(t.getIp());
        c.setPort(t.getPort());
        c.setUsr(t.getUsr());
        c.setPsword(t.getPsword());
        return c;
    }

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

    public CanalConnector getCanalConn() {
        return canalConn;
    }

    public void setCanalConn(CanalConnector canalConn) {
        this.canalConn = canalConn;
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

    public String getFilter() {
        return String.join(",", getFilterList());
    }


    public List<String> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<String> filterList) {
        this.filterList = filterList;
    }

    public List<String> getFullDumpList() {
        return fullDumpList;
    }

    public void setFullDumpList(List<String> fullDumpList) {
        this.fullDumpList = fullDumpList;
    }

    public void appendFullDumpTable(String dbTable) {
        fullDumpList.add(dbTable);
    }

    public String getFullDumpStr() {
        return String.join(",", fullDumpList);
    }

    public boolean needFullPull() {
        return !fullDumpList.isEmpty();
    }

    // 由ip+" "+port生产canal destination
    public String getDestination() {
        return ip + "_" + port;
    }

    public boolean appendFilter(String filter) {
        if (filterList.contains(filter)) {
            return false;
        } else {
            return filterList.add(filter);
        }
    }

    public void removeFilter(String filter) {
        this.filterList.remove(filter);
    }

    public boolean readyToRemoveInstance() {
        return filterList.size() == 0;
    }
}
