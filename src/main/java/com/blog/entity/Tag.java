package com.blog.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Tag {
    private Long tagId;

    private String tagName;


    private Date createTime;

    private Date updateTime;


}
