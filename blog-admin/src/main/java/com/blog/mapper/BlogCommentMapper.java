package com.blog.mapper;

import com.blog.util.bo.BlogCommentBo;
import com.blog.entity.BlogComment;

import java.util.List;

/**
 * @author: zhang
 * @time: 2024-09-14 11:10
 */
public interface BlogCommentMapper {
    /**
     * 根据主键删除
     * @param commentId 评论id
     * @return int
     */
    int deleteByPrimaryKey(Integer commentId);

    /**
     * 插入评论信息
     * @param record 评论信息
     * @return int
     */
    int insert(BlogComment record);
    /**
     * 根据输入选择性地插入评论信息
     * @param record 评论信息
     * @return int
     */
    int insertSelective(BlogComment record);

    /**
     * 根据主键查询评论信息
     * @param commentId 评论主键
     * @return BlogComment
     */
    BlogComment selectByPrimaryKey(Integer commentId);

    /**
     * 根据主键选择性地更新评论信息
     * @param record  评论信息
     * @return int
     */
    int updateByPrimaryKeySelective(BlogComment record);

    /**
     * 根据主键更新评论信息
     * @param record 评论信息
     * @return int
     */
    int updateByPrimaryKey(BlogComment record);

    /**
     * 根据博客id查询评论列表
     * @param blogId 博客id
     * @return List<BlogComment>
     */
    List<BlogComment> selectByBlogId(Long blogId);

    /**
     * 根据博客id查询评论数量
     * @param blogId 博客id
     * @return int
     */
    int selectCommentCountByBlogId(Long blogId);

    /**
     * 根据博客id查询一级评论列表
     * @param blogId 博客id
     * @return List<BlogCommentBo>
     */
    List<BlogCommentBo> queryFirstCommentList(Long blogId);

    /**
     * 根据博客id查询二级评论列表
     * @param blogId 博客id
     * @return List<BlogCommentBo>
     */
    List<BlogCommentBo> querySecondCommentList(Long blogId);
}