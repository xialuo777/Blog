package com.blog.exception;

import com.blog.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = {"com.blog.controller","com.blog.service"})
public class UserExceptionControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseResult<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseResult.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMessage());
    }


    /**
     * 自定义异常统一管理
     * @Param e
     * @Return ResponseResult
     */
    @ExceptionHandler(value= BusinessException.class)
    public ResponseResult handleBusinessException(BusinessException e){
        log.error("错误：",e);
        return ResponseResult.fail(e.getCode(),e.getMessage());
    }

}

