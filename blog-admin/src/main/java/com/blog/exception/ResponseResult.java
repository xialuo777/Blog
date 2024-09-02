package com.blog.exception;

import com.blog.enums.ErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "结果返回类")
public class ResponseResult<T> {

    /**
     * 返回状态
     */
    @ApiModelProperty(value = "返回状态")
    private Boolean flag;

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

    public static <T> ResponseResult<T> success() {
        return buildResult(true, null, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage());
    }

    public static <T> ResponseResult<T> success(T data) {
        return buildResult(true, data, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage());
    }

    public static <T> ResponseResult<T> fail(String message) {
        return buildResult(false, null, ErrorCode.FAIL.getCode(), message);
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return buildResult(false, null, code, message);
    }

    private static <T> ResponseResult<T> buildResult(Boolean flag, T data, Integer code, String message) {
        ResponseResult<T> r = new ResponseResult<>();
        r.setFlag(flag);
        r.setData(data);
        r.setCode(code);
        r.setMsg(message);
        return r;
    }

}