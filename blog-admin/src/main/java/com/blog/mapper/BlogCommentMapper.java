package com.blog.mapper;

import com.blog.entity.BlogComment;
import com.blog.util.bo.BlogCommentBo;

import java.util.List;

public interface BlogCommentMapper {
    int deleteByPrimaryKey(Long commentId);

    int insert(BlogComment record);

    int insertSelective(BlogComment record);

    BlogComment selectByPrimaryKey(Long commentId);

    int updateByPrimaryKeySelective(BlogComment record);

    int updateByPrimaryKey(BlogComment record);

    /**
     * 根据博客id查询评论列表
     *
     * @param blogId 博客id
     * @return List<BlogComment>
     */
    List<BlogComment> selectByBlogId(Long blogId);

    /**
     * 根据博客id查询评论数量
     *
     * @param blogId 博客id
     * @return int
     */
    int selectCommentCountByBlogId(Long blogId);

    /**
     * 根据博客id查询一级评论列表
     *
     * @param blogId 博客id
     * @return List<BlogCommentBo>
     */
    List<BlogCommentBo> queryFirstCommentList(Long blogId);

    /**
     * 根据博客id查询二级评论列表
     *
     * @param blogId 博客id
     * @return List<BlogCommentBo>
     */
    List<BlogCommentBo> querySecondCommentList(Long blogId);
}