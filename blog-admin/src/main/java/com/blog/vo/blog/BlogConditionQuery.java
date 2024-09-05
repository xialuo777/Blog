package com.blog.vo.blog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "博客查询条件")
public class BlogConditionQuery {
    @ApiModelProperty(value = "博客id")
    private Integer id;
    @ApiModelProperty(value = "博客标题")
    private String blogTitle;
    @ApiModelProperty(value = "博客分类")
    private BlogCategory category;

}
