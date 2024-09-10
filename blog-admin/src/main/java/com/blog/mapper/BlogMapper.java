package com.blog.mapper;

import com.blog.entity.Blog;

import java.util.List;

public interface BlogMapper {
    int deleteByPrimaryKey(Long blogId);

    int insert(Blog record);

    int insertSelective(Blog record);

    Blog selectByPrimaryKey(Long blogId);

    int updateByPrimaryKeySelective(Blog record);

    int updateByPrimaryKey(Blog record);

    List<Blog> selectListByUserId(Long userId);

    List<Blog> selectListByCategoryId(Long categoryId);
}