package com.blog.enums;

/**
 * 错误类型枚举
 * @author zhang
 */
public enum ErrorCode {
    //系统异常
    SYSTEM_ERROR(10000,"系统异常"),
    //输入参数校验失败
    PARAMS_ERROR(10001,"参数校验失败"),
    //用户不存在
    USER_NOT_FOUND(10002,"用户不存在"),
    //该邮箱已被注册
    USER_ALREADY_EXISTS(10003,"该邮箱已被注册"),
    //用户密码错误
    INVALID_CREDENTIALS(10004,"用户密码错误"),
    //token校验出现异常
    TOKEN_ERROR(10005,"token出现异常"),
    //操作失败
    FAIL(10006,"操作失败"),
    //token过期
    TOKEN_EXPIRED(10007,"token已过期"),
    //操作成功
    SUCCESS(20000,"操作成功");
    private final int code;
    private final String message;

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
