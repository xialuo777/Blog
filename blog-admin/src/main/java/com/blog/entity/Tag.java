package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

@ApiModel(description = "tag")
public class Tag {
    @ApiModelProperty(value = "标签id")
    private Integer tagId;

    @ApiModelProperty(value = "标签名称")
    private String tagName;

    @ApiModelProperty(value = "标签创建时间")
    private Date createTime;

    @ApiModelProperty(value = "是否删除该分类 0 否 1 是")
    private Integer deleteFlag;

    public Tag(String tagName) {
        this.tagName = tagName;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}