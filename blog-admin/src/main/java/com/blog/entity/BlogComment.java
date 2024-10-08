package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@ApiModel(description = "blog_comment")
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private Long lastId;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @ApiModelProperty(value = "是否删除 0-未删除 1-已删除")
    private Byte isDeleted;

    public BlogComment(long commentId, long blogId, String comentator, long commentatorId, String content) {
        this.commentId = commentId;
        this.blogId = blogId;
        this.commentator = comentator;
        this.commentatorId = commentatorId;
        this.commentBody = content;
    }
}