package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@ApiModel(description = "tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @ApiModelProperty(value = "")
    private Long tagId;

    @ApiModelProperty(value = "")
    private String tagName;

    @ApiModelProperty(value = "")
    private Date createTime;

    @ApiModelProperty(value = "")
    private Integer deleteFlag;

    public Tag(long nextId, String tagName) {
        this.tagId = nextId;
        this.tagName = tagName;
    }
}