package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "tag")
@Data
@AllArgsConstructor
public class Tag {
    @ApiModelProperty(value = "标签id")
    private Integer tagId;

    @ApiModelProperty(value = "标签名称")
    private String tagName;

    @ApiModelProperty(value = "标签创建时间")
    private Date createTime;

    @ApiModelProperty(value = "是否删除该分类 0 否 1 是")
    private Integer deleteFlag;

    public Tag(String tagName){
        this.tagName = tagName;
    }

}