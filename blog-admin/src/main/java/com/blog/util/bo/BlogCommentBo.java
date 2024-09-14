package com.blog.util.bo;

import com.blog.entity.BlogComment;
import com.blog.entity.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: zhang
 * @time: 2024-09-14 11:32
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BlogCommentBo extends BlogComment {
    @ApiModelProperty(value = "评论人信息")
    private User user;
    @ApiModelProperty(value = "下一条回复")
    private List<BlogCommentBo> nextNodes = new ArrayList<>();

    public BlogCommentBo(BlogCommentBo blogCommentBo) {
        super();
        setCommentId(blogCommentBo.getCommentId());
        setBlogId(blogCommentBo.getBlogId());
        setCommentatorId(blogCommentBo.getCommentatorId());
        setCommentator(blogCommentBo.getCommentator());
        setCommentBody(blogCommentBo.getCommentBody());
        setCommentCreateTime(blogCommentBo.getCommentCreateTime());
        setLastId(blogCommentBo.getLastId());
        setIsDeleted(blogCommentBo.getIsDeleted());
        this.user = blogCommentBo.getUser();
    }

}
