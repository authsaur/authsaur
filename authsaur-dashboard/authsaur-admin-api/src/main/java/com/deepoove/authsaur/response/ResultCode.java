package com.deepoove.authsaur.response;

import java.text.MessageFormat;

public enum ResultCode {

    // @formatter:off

    SUCCESS(200,"SUCCESS"),
    FAIL(400, "FAIL");

    // @formatter:on

    private int code;
    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String format(Object... args) {
        return null == args ? this.message : MessageFormat.format(this.message, args);
    }
}