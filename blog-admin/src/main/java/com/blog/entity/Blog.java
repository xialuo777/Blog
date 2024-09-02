package com.blog.entity;

import lombok.Data;

import java.util.Date;
@Data
public class Blog {
    private Long blogId;

    private String blogTitle;

    private Long userId;

    private String blogDesc;

    private String blogContent;

    private Long categoryId;

    private Integer blogStatus;

    private String blogTags;

    private String thumbnail;

    private Long viewCount;

    private Date creatTime;

    private Date updateTime;

    private Integer isTop;

}