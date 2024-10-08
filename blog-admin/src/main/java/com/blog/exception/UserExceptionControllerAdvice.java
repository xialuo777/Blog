package com.blog.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhang
 * @time: 2024-09-14 10:56
 */
@Slf4j
@RestControllerAdvice(basePackages = {"com.blog.controller","com.blog.service"})
public class UserExceptionControllerAdvice {
    /**
     *
     * @param ex 参数异常类型
     * @return ResponseResult
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseResult<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>(1);
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseResult.fail(errors.toString());
    }


    /**
     * 自定义异常统一管理
     * @param e 错误异常类型
     * @return ResponseResult
     */
    @ExceptionHandler(value= BusinessException.class)
    public ResponseResult<Object> handleBusinessException(BusinessException e){
        log.error("错误：",e);
        return ResponseResult.fail(e.getCode(),e.getMessage());
    }

}

