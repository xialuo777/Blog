package com.blog.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Category {

    private Long categoryId;

    private String categoryName;

    private Date createTime;

    private Date updateTime;
}
