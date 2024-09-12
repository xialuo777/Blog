package com.blog.service;

import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.BlogComment;
import com.blog.mapper.BlogCommentMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final BlogCommentMapper blogCommentMapper;
    public void addComment(BlogComment blogComment) {
        blogCommentMapper.insertSelective(blogComment);
    }

    public Optional<BlogComment> selectCommentById(Integer commentId) {
        return Optional.ofNullable(blogCommentMapper.selectByPrimaryKey(commentId));
    }

    public void deleteComment(Integer commentId) {
        blogCommentMapper.deleteByPrimaryKey(commentId);
    }

    public PageResult<BlogComment> getCommentList(PageRequest pageRequest, Long blogId) {
        int pageSize = pageRequest.getPageSize();
        int pageNo = pageRequest.getPageNo();
        PageHelper.startPage(pageNo, pageSize);
        List<BlogComment> blogCommentList = blogCommentMapper.selectByBlogId(blogId);
        int totalCount = blogCommentMapper.selectCommentCountByBlogId(blogId);
        return new PageResult<>(blogCommentList, totalCount);
    }
}
