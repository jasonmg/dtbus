package com.hawker.pojo;

/**
 * Created by 金皓天 on 2017/7/13.
 */
public class Result {
    private String code;
    private String errmsg;
    private Object data;
    private Integer rows;

    public Result(String code, String errmsg) {
        this.code = code;
        this.errmsg = errmsg;
    }

    public static Result succeed(Object data) {
        Result r = new Result("0", "");
        r.data = data;
        return r;
    }

    public static Result succeed() {
        return succeed(null);
    }


    public static Result failed(int code, String msg) {
        return new Result(code + "", msg);
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
