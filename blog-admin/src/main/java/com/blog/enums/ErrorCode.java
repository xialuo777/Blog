package com.blog.enums;

public enum ErrorCode {
    SYSTEM_ERROR(10000,"系统异常"),
    PARAMS_ERROR(10001,"参数校验失败"),
    USER_NOT_FOUND(10002,"用户不存在"),
    USER_ALREADY_EXISTS(10003,"用户已存在"),
    INVALID_CREDENTIALS(10004,"用户密码错误"),
    TOKEN_ERROR(10005,"token出现异常"),
    FAIL(10006,"失败"),
    TOKEN_EXPIRED(10007,"token已过期"),
    SUCCESS(20000,"成功");
    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
