package com.deepoove.authsaur.response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Result<T> {
    protected int code;
    protected T data;
    protected boolean success;
    protected String msg;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<T>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setData(data);
        r.setSuccess(true);
        r.setMsg(ResultCode.SUCCESS.getMessage());
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<Map<String, List<T>>> successWithList(List<T> data) {
        return success(Collections.singletonMap("list", data));
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<T>();
        r.setCode(code);
        r.setSuccess(false);
        r.setMsg(msg);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(ResultCode.FAIL.getCode(), msg);
    }

    public static <T> Result<T> fail(ResultCode resultCode, Object... args) {
        if (resultCode == ResultCode.SUCCESS) {
            throw new IllegalArgumentException("illegal fail result.");
        }
        Result<T> r = new Result<T>();
        r.setCode(resultCode.getCode());
        r.setSuccess(false);
        r.setMsg(resultCode.format(args));
        return r;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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