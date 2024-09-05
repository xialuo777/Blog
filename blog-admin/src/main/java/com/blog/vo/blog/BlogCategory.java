package com.blog.vo.blog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "博客分类")
public class BlogCategory {
    /*分类id*/
    @ApiModelProperty(value = "分类id")
    private Integer id;

    /* 分类名*/
    @ApiModelProperty(value = "分类名")
    private String categoryName;
}
