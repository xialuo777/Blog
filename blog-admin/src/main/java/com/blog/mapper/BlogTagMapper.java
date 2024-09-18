package com.blog.mapper;

import com.blog.entity.BlogTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BlogTagMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BlogTag record);

    int insertSelective(BlogTag record);

    BlogTag selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BlogTag record);

    int updateByPrimaryKey(BlogTag record);

    /**
     * 批量插入博客标签关联信息
     *
     * @param blogTags 博客标签关联信息
     * @return int
     */
    int insertList(@Param("relationList") List<BlogTag> blogTags);

    /**
     * 根据博客id查询博客标签关联信息
     *
     * @param blogId 博客id
     * @return List<BlogTag>
     */
    List<BlogTag> selectListByBlogId(Long blogId);

    /**
     * 根据博客id删除博客标签关联信息
     *
     * @param blogId 博客id
     * @return int
     */
    int deleteByBlogId(Long blogId);
}