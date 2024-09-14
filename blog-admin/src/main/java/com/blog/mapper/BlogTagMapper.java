package com.blog.mapper;

import com.blog.entity.BlogTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: zhang
 * @time: 2024-09-14 11:17
 */
public interface BlogTagMapper {
    /**
     * 根据主键删除
     * @param id 博客标签关联id
     * @return int
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 插入博客标签关联信息
     * @param record 博客标签关联信息
     * @return int
     */
    int insert(BlogTag record);

    /**
     * 选择性地插入博客标签关联信息
     * @param record 博客标签关联信息
     * @return int
     */
    int insertSelective(BlogTag record);

    /**
     * 根据主键查询博客标签关联信息
     * @param id 博客标签关联id
     * @return BLogTag
     */
    BlogTag selectByPrimaryKey(Integer id);

    /**
     * 根据主键选择性地更新博客标签关联信息
     * @param record 博客标签关联信息
     * @return int
     */
    int updateByPrimaryKeySelective(BlogTag record);

    /**
     * 根据主键更新博客标签关联信息
     * @param record 博客标签关联信息
     * @return int
     */
    int updateByPrimaryKey(BlogTag record);

    /**
     * 批量插入博客标签关联信息
     * @param blogTags 博客标签关联信息
     * @return int
     */
    int insertList(@Param("relationList") List<BlogTag> blogTags);

    /**
     * 根据博客id查询博客标签关联信息
     * @param blogId 博客id
     * @return List<BlogTag>
     */
    List<BlogTag> selectListByBlogId(Long blogId);

    /**
     * 根据博客id删除博客标签关联信息
     * @param blogId 博客id
     */
    void deleteByBlogId(Long blogId);
}