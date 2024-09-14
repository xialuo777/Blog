package com.blog.exception;

import com.blog.enums.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 * @author zhang
 * @time 2024-08-23 16:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException{

    private final long serialVersionUID = 1L;

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 异常对应的返回码
     */
    private Integer code;

    /**
     * 异常对应的描述信息
     */
    private String message;

    private Throwable throwable;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = String.format("%s %s", message, cause.getMessage());
    }

    public BusinessException(int code, String message, Throwable throwable) {
        super(message);
        this.code = code;
        this.message = message;
        this.throwable = throwable;
    }

    public BusinessException(ErrorCode resError) {
        this(resError.getCode(), resError.getMessage(), null);
    }

    public BusinessException(ErrorCode resError, Throwable throwable) {
        this(resError.getCode(), resError.getMessage(), throwable);
    }

    public BusinessException(ErrorCode resError, Object... args) {
        super(resError.getMessage());
        String message = resError.getMessage();
        try {
            message =
                    String.format("%s %s", resError.getMessage(), OBJECT_MAPPER.writeValueAsString(args));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        this.message = message;
        this.code = resError.getCode();
    }

    public Integer getCode() {
        return code;
    }


    @Override
    public String getMessage() {
        return message;
    }




}