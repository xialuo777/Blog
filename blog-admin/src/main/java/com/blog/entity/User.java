package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "`user`")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @ApiModelProperty(value = "")
    private Long userId;

    @ApiModelProperty(value = "")
    private String account;

    @ApiModelProperty(value = "")
    private String nickName;

    @ApiModelProperty(value = "")
    private String password;

    @ApiModelProperty(value = "")
    private String email;

    @ApiModelProperty(value = "")
    private String phone;

    @ApiModelProperty(value = "")
    private Integer status;

    @ApiModelProperty(value = "")
    private String website;

}