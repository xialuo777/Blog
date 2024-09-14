package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: zhang
 * @time: 2024-09-14 10:46
 */
@ApiModel(description="`admin`")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    @ApiModelProperty(value="管理员id")
    private Long adminId;

    @ApiModelProperty(value="管理员账号")
    private String account;

    @ApiModelProperty(value="管理员密码")
    private String password;
}