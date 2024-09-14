package com.blog.mapper;

import com.blog.entity.Blog;
import com.blog.vo.blog.BlogDesc;

import java.util.List;

public interface BlogMapper {
    int deleteByPrimaryKey(Long blogId);

    int insert(Blog record);

    int insertSelective(Blog record);

    Blog selectByPrimaryKey(Long blogId);

    int updateByPrimaryKeySelective(Blog record);

    int updateByPrimaryKey(Blog record);

    /**
     * 根据用户id查询博客列表
     *
     * @param userId 用户id
     * @return List<Blog>
     */
    List<Blog> selectListByUserId(Long userId);

    /**
     * 根据分类id查询博客列表
     *
     * @param categoryId 分类id
     * @return List<Blog>
     */
    List<Blog> selectListByCategoryId(Long categoryId);

    /**
     * 查询所有博客列表
     *
     * @return List<Blog>
     */
    List<BlogDesc> selectList();
}