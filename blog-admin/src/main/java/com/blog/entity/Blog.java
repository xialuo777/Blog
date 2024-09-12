package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

import java.util.Date;

@ApiModel(description = "blog")
@Data
public class Blog {
    @ApiModelProperty(value = "博客id")
    private Long blogId;

    @ApiModelProperty(value = "博客标题")
    private String blogTitle;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "博客简介")
    private String blogDesc;

    @ApiModelProperty(value = "博客内容")
    private String blogContent;

    @ApiModelProperty(value = "分类id")
    private Integer categoryId;

    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @ApiModelProperty(value = "状态 (1公开 2私密 3草稿)", required = true)
    private Integer blogStatus;

    @ApiModelProperty(value = "博客标签")
    private String blogTags;

    @ApiModelProperty(value = "缩略图")
    private String thumbnail;

    @ApiModelProperty(value = "浏览量")
    private Long viewCount;

    @ApiModelProperty(value = "创建时间")
    private Date creatTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否置顶 (0否 1是)", required = true)
    private Integer isTop;

    @ApiModelProperty(value = "是否允许评论 (0是 1否)")
    private Integer enableComment;

    @ApiModelProperty(value = "是否删除 (0否 1是)")
    private Integer isDelete;

    @ApiModelProperty(value = "博客网址")
    private String subUrl;

}