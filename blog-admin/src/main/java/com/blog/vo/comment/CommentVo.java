package com.blog.vo.comment;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


@ApiModel(description = "blog_comment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVo {
    /**
     * 关联的blog主键
     */
    @ApiModelProperty(value = "关联的blog主键")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long blogId;

    /**
     * 评论人的id
     */
    @ApiModelProperty(value = "评论人的id")
    private Long commentatorId;

    /**
     * 评论内容
     */
    @ApiModelProperty(value = "评论内容")
    @Length(min = 1, max = 200, message = "评论内容长度在1-200位")
    private String commentBody;

}