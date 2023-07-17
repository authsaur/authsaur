package com.deepoove.authsaur.rest.model;

public class Result<T> {
    private T data;
    private boolean success;
    private String msg;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<T>();
        r.setData(data);
        r.setSuccess(true);
        r.setMsg("success");
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<T>();
        r.setSuccess(false);
        r.setMsg(msg);
        return r;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
