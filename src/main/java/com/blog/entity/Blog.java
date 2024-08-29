package com.blog.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class Blog {
    /*博客id*/
    private Long blogId;
    /*博客作者*/
    private Long userId;
    /*博客标题*/
    private String blogTitle;
    /*博客摘要*/
    private String articleDesc;
    /*博客分类*/
    private Long categoryId;


    /*状态 (1 公开 2 私密  3 撰写)*/
    private Integer blogStatus;

    /*发表时间*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /*更新时间*/
    private Date updateTime;

    private String blogContent;



}