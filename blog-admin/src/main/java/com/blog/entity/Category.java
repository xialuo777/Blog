package com.blog.entity;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "category")
@Data
public class Category {
    @ApiModelProperty(value = "博客分类id")
    private Integer categoryId;

    @ApiModelProperty(value = "博客分类名称")
    private String categoryName;

    @ApiModelProperty(value = "博客分类排序，即分类列表的显示顺序，值越低显示越靠前")
    private Integer categoryRank;

    @ApiModelProperty(value = "分类创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "是否删除该分类 0 否 1 是")
    private Integer deleteFlag;

    public Category(Integer categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

}