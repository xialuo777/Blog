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
    private Integer commentId;

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
     * 回复内容
     */
    @ApiModelProperty(value = "回复内容")
    private String replyBody;

    /**
     * 回复时间
     */
    @ApiModelProperty(value = "回复时间")
    private Date replyCreateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @ApiModelProperty(value = "是否删除 0-未删除 1-已删除")
    private Byte isDeleted;

}