package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "`user`")
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}