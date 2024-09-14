package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: zhang
 * @time: 2024-09-14 10:47
 */
@ApiModel(description = "blog_tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogTag {
    @ApiModelProperty(value = "博客标签关联id")
    private Integer id;

    @ApiModelProperty(value = "博客id")
    private Long blogId;

    @ApiModelProperty(value = "标签id")
    private Integer tagId;

    public BlogTag(Long blogId, Integer tagId) {
        this.blogId = blogId;
        this.tagId = tagId;
    }
}