package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: zhang
 * @time: 2024-09-14 10:48
 */
@ApiModel(description = "tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
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