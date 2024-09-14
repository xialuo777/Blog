package com.blog.mapper;

import com.blog.entity.Blog;
import com.blog.vo.blog.BlogDesc;

import java.util.List;

/**
 * @author: zhang
 * @time: 2024-09-14 11:13
 */
public interface BlogMapper {
    /**
     * 根据主键删除
     * @param blogId 博客id
     * @return int
     */
    int deleteByPrimaryKey(Long blogId);

    /**
     * 插入博客信息
     * @param record 博客信息
     * @return int
     */
    int insert(Blog record);

    /**
     * 选择性地插入博客信息
     * @param record 博客信息
     * @return int
     */
    int insertSelective(Blog record);

    /**
     * 根据主键查询博客信息
     * @param blogId 博客id
     * @return Blog
     */
    Blog selectByPrimaryKey(Long blogId);

    /**
     * 根据主键选择性地更新博客
     * @param record 博客信息
     * @return int
     */
    int updateByPrimaryKeySelective(Blog record);

    /**
     * 根据主键更新博客信息
     * @param record 博客信息
     * @return int
     */
    int updateByPrimaryKey(Blog record);

    /**
     * 根据用户id查询博客列表
     * @param userId 用户id
     * @return List<Blog>
     */
    List<Blog> selectListByUserId(Long userId);

    /**
     * 根据分类id查询博客列表
     * @param categoryId 分类id
     * @return List<Blog>
     */
    List<Blog> selectListByCategoryId(Long categoryId);

    /**
     * 查询所有博客列表
     * @return List<Blog>
     */
    List<BlogDesc> selectList();
}