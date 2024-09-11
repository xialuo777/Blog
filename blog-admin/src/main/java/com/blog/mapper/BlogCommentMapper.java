package com.blog.mapper;

import com.blog.entity.BlogComment;

public interface BlogCommentMapper {
    int deleteByPrimaryKey(Integer commentId);

    int insert(BlogComment record);

    int insertSelective(BlogComment record);

    BlogComment selectByPrimaryKey(Integer commentId);

    int updateByPrimaryKeySelective(BlogComment record);

    int updateByPrimaryKey(BlogComment record);
}