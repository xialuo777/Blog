package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "blog_tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogTag {
    @ApiModelProperty(value = "")
    private Long id;

    @ApiModelProperty(value = "")
    private Long blogId;

    @ApiModelProperty(value = "")
    private Long tagId;
}