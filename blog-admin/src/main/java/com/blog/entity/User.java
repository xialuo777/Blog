package com.blog.entity;

import lombok.Data;


@Data
public class User {
    private Long userId;
    private String account;
    private String nickName;
    private String password;
    private String email;
    private String phone;
}
