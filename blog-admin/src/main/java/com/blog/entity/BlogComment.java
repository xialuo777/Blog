package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

@ApiModel(description = "blog_comment")
public class BlogComment {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private Long commentId;

    /**
     * 关联的blog主键
     */
    @ApiModelProperty(value = "关联的blog主键")
    private Long blogId;

    /**
     * 评论者名称
     */
    @ApiModelProperty(value = "评论者名称")
    private String commentator;

    /**
     * 评论人的id
     */
    @ApiModelProperty(value = "评论人的id")
    private Long commentatorId;

    /**
     * 评论内容
     */
    @ApiModelProperty(value = "评论内容")
    private String commentBody;

    /**
     * 评论提交时间
     */
    @ApiModelProperty(value = "评论提交时间")
    private Date commentCreateTime;

    /**
     * 评论上一级的id
     */
    @ApiModelProperty(value = "评论上一级的id")
    private Integer lastId;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @ApiModelProperty(value = "是否删除 0-未删除 1-已删除")
    private Byte isDeleted;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public String getCommentator() {
        return commentator;
    }

    public void setCommentator(String commentator) {
        this.commentator = commentator;
    }

    public Long getCommentatorId() {
        return commentatorId;
    }

    public void setCommentatorId(Long commentatorId) {
        this.commentatorId = commentatorId;
    }

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public Date getCommentCreateTime() {
        return commentCreateTime;
    }

    public void setCommentCreateTime(Date commentCreateTime) {
        this.commentCreateTime = commentCreateTime;
    }

    public Integer getLastId() {
        return lastId;
    }

    public void setLastId(Integer lastId) {
        this.lastId = lastId;
    }

    public Byte getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Byte isDeleted) {
        this.isDeleted = isDeleted;
    }
}