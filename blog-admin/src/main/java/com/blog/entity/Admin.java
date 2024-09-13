package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description="`admin`")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    @ApiModelProperty(value="")
    private Long adminId;

    @ApiModelProperty(value="")
    private String account;

    @ApiModelProperty(value="")
    private String password;
}