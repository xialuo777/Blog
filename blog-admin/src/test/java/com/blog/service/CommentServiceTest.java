package com.blog.service;

import com.blog.util.bo.BlogCommentBo;
import com.blog.util.dto.PageRequest;
import com.blog.entity.BlogComment;
import com.blog.mapper.BlogCommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
    void queryCommentList_ReturnsList() {
        PageRequest pageRequest = new PageRequest(1, 10);
        Long blogId = 1L;

        List<BlogCommentBo> firstCommentList = new ArrayList<>();
        BlogCommentBo firstComment = new BlogCommentBo();
        firstComment.setCommentId(1);
        firstComment.setLastId(0);
        firstCommentList.add(firstComment);

        List<BlogCommentBo> secondCommentList = new ArrayList<>();
        BlogCommentBo secondComment = new BlogCommentBo();
        secondComment.setCommentId(2);
        secondComment.setLastId(1);
        secondCommentList.add(secondComment);

        when(mockBlogCommentMapper.queryFirstCommentList(blogId)).thenReturn(firstCommentList);
        when(mockBlogCommentMapper.querySecondCommentList(blogId)).thenReturn(secondCommentList);

        List<BlogCommentBo> result = commentServiceUnderTest.queryCommentList(pageRequest, blogId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCommentId());
        assertEquals(1, result.get(0).getNextNodes().size());
        assertEquals(2, result.get(0).getNextNodes().get(0).getCommentId());

        verify(mockBlogCommentMapper, times(1)).queryFirstCommentList(blogId);
        verify(mockBlogCommentMapper, times(1)).querySecondCommentList(blogId);
    }

    @Test
    void queryCommentList_ReturnsEmptyList() {
        PageRequest pageRequest = new PageRequest(1, 10);
        Long blogId = 1L;

        when(mockBlogCommentMapper.queryFirstCommentList(blogId)).thenReturn(new ArrayList<>());
        when(mockBlogCommentMapper.querySecondCommentList(blogId)).thenReturn(new ArrayList<>());

        List<BlogCommentBo> result = commentServiceUnderTest.queryCommentList(pageRequest, blogId);

        assertEquals(0, result.size());

        verify(mockBlogCommentMapper, times(1)).queryFirstCommentList(blogId);
        verify(mockBlogCommentMapper, times(1)).querySecondCommentList(blogId);
    }

    @Test
    void queryCommentList_ReturnsListWithUnlinkedComments() {

        PageRequest pageRequest = new PageRequest(1, 10);
        Long blogId = 1L;

        List<BlogCommentBo> firstCommentList = new ArrayList<>();
        BlogCommentBo firstComment = new BlogCommentBo();
        firstComment.setCommentId(1);
        firstComment.setLastId(0);
        firstCommentList.add(firstComment);

        List<BlogCommentBo> secondCommentList = new ArrayList<>();
        BlogCommentBo secondComment = new BlogCommentBo();
        secondComment.setCommentId(2);
        secondComment.setLastId(1);
        secondCommentList.add(secondComment);
        BlogCommentBo unlinkedComment = new BlogCommentBo();
        unlinkedComment.setCommentId(3);
        unlinkedComment.setLastId(2);
        secondCommentList.add(unlinkedComment);

        when(mockBlogCommentMapper.queryFirstCommentList(blogId)).thenReturn(firstCommentList);
        when(mockBlogCommentMapper.querySecondCommentList(blogId)).thenReturn(secondCommentList);

        List<BlogCommentBo> result = commentServiceUnderTest.queryCommentList(pageRequest, blogId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCommentId());
        assertEquals(1, result.get(0).getNextNodes().size());
        assertEquals(2, result.get(0).getNextNodes().get(0).getCommentId());
        assertEquals(1, result.get(0).getNextNodes().get(0).getNextNodes().size());
        assertEquals(3, result.get(0).getNextNodes().get(0).getNextNodes().get(0).getCommentId());

        verify(mockBlogCommentMapper, times(1)).queryFirstCommentList(blogId);
        verify(mockBlogCommentMapper, times(1)).querySecondCommentList(blogId);
    }

}
