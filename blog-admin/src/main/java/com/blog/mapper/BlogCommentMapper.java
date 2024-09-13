package com.blog.mapper;

import com.blog.bo.BlogCommentBo;
import com.blog.entity.BlogComment;

import java.util.List;

public interface BlogCommentMapper {
    int deleteByPrimaryKey(Integer commentId);

    int insert(BlogComment record);

    int insertSelective(BlogComment record);

    BlogComment selectByPrimaryKey(Integer commentId);

    int updateByPrimaryKeySelective(BlogComment record);

    int updateByPrimaryKey(BlogComment record);

    List<BlogComment> selectByBlogId(Long blogId);

    int selectCommentCountByBlogId(Long blogId);


    List<BlogCommentBo> queryFirstCommentList(Long blogId);
    List<BlogCommentBo> querySecondCommentList(Long blogId);
}