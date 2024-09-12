package com.blog.service;

import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.BlogComment;
import com.blog.mapper.BlogCommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private BlogCommentMapper mockBlogCommentMapper;

    private CommentService commentServiceUnderTest;

    @BeforeEach
    void setUp() {
        commentServiceUnderTest = new CommentService(mockBlogCommentMapper);
    }

    @Test
    void addComment() {
        final BlogComment blogComment = new BlogComment();
        blogComment.setCommentId(0);
        blogComment.setBlogId(0L);
        blogComment.setCommentator("commentator");
        blogComment.setCommentatorId(0L);
        blogComment.setCommentBody("commentBody");

        commentServiceUnderTest.addComment(blogComment);

        final BlogComment record = new BlogComment();
        record.setCommentId(0);
        record.setBlogId(0L);
        record.setCommentator("commentator");
        record.setCommentatorId(0L);
        record.setCommentBody("commentBody");
        verify(mockBlogCommentMapper).insertSelective(record);
    }

    @Test
    void selectCommentById() {
        final BlogComment blogComment = new BlogComment();
        blogComment.setCommentId(0);
        blogComment.setBlogId(0L);
        blogComment.setCommentator("commentator");
        blogComment.setCommentatorId(0L);
        blogComment.setCommentBody("commentBody");
        final Optional<BlogComment> expectedResult = Optional.of(blogComment);

        final BlogComment blogComment1 = new BlogComment();
        blogComment1.setCommentId(0);
        blogComment1.setBlogId(0L);
        blogComment1.setCommentator("commentator");
        blogComment1.setCommentatorId(0L);
        blogComment1.setCommentBody("commentBody");
        when(mockBlogCommentMapper.selectByPrimaryKey(0)).thenReturn(blogComment1);

        final Optional<BlogComment> result = commentServiceUnderTest.selectCommentById(0);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void selectCommentById_Without_Comment() {
        when(mockBlogCommentMapper.selectByPrimaryKey(0)).thenReturn(null);

        final Optional<BlogComment> result = commentServiceUnderTest.selectCommentById(0);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteComment() {

        commentServiceUnderTest.deleteComment(0);

        verify(mockBlogCommentMapper).deleteByPrimaryKey(0);
    }

    @Test
    void getCommentList() {

        final PageRequest pageRequest = new PageRequest(0, 0);
        final BlogComment blogComment = new BlogComment();
        blogComment.setCommentId(0);
        blogComment.setBlogId(0L);
        blogComment.setCommentator("commentator");
        blogComment.setCommentatorId(0L);
        blogComment.setCommentBody("commentBody");
        final PageResult<BlogComment> expectedResult = new PageResult<>(Arrays.asList(blogComment), 1);

        final BlogComment blogComment1 = new BlogComment();
        blogComment1.setCommentId(0);
        blogComment1.setBlogId(0L);
        blogComment1.setCommentator("commentator");
        blogComment1.setCommentatorId(0L);
        blogComment1.setCommentBody("commentBody");
        final List<BlogComment> blogCommentList = Arrays.asList(blogComment1);
        when(mockBlogCommentMapper.selectByBlogId(0L)).thenReturn(blogCommentList);

        when(mockBlogCommentMapper.selectCommentCountByBlogId(0L)).thenReturn(1);

        final PageResult<BlogComment> result = commentServiceUnderTest.getCommentList(pageRequest, 0L);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getCommentList_Without_Comment() {
        final PageRequest pageRequest = new PageRequest(1, 2);
        final BlogComment blogComment = new BlogComment();
        blogComment.setCommentId(0);
        blogComment.setBlogId(1L);
        blogComment.setCommentator("commentator");
        blogComment.setCommentatorId(2L);
        blogComment.setCommentBody("commentBody");
        final PageResult<BlogComment> expectedResult = new PageResult<>(Collections.emptyList(), 0);
        when(mockBlogCommentMapper.selectByBlogId(0L)).thenReturn(Collections.emptyList());
        when(mockBlogCommentMapper.selectCommentCountByBlogId(0L)).thenReturn(0);

        final PageResult<BlogComment> result = commentServiceUnderTest.getCommentList(pageRequest, 0L);

        assertThat(result).isEqualTo(expectedResult);
    }
}
