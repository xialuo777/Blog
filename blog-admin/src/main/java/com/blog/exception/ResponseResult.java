package com.blog.exception;

import com.blog.enums.ErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author: zhang
 * @time: 2024-09-14 10:56
 * @param <T>
 */
@Data
@ApiModel(description = "结果返回类")
public class ResponseResult<T> {


    /**
     * 状态码
     */
    @ApiModelProperty(value = "状态码")
    private Integer code;

    /**
     * 返回信息
     */
    @ApiModelProperty(value = "返回信息")
    private String msg;

    /**
     * 返回数据
     */
    @ApiModelProperty(value = "返回数据")
    private T data;



    public static <T> ResponseResult<T> success(T data) {
        return buildResult( data, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage());
    }

    public static <T> ResponseResult<T> fail(String message) {
        return buildResult( null, ErrorCode.FAIL.getCode(), message);
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return buildResult( null, code, message);
    }


    private static <T> ResponseResult<T> buildResult( T data, Integer code, String message) {
        ResponseResult<T> r = new ResponseResult<>();
        r.setData(data);
        r.setCode(code);
        r.setMsg(message);
        return r;
    }

}