package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.util.JwtProcessor;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.comment.CommentInVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Extensions({@ExtendWith(SpringExtension.class), @ExtendWith(OutputCaptureExtension.class)})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentControllerTest {
    @MockBean
    private RedisProcessor redisProcessor;
    @MockBean
    private JwtProcessor jwtProcessor;
    @MockBean
    private CurrentUserHolder currentUserHolder;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("添加评论")
    void addComment1() {
        CommentInVo commentInVo = new CommentInVo(1L, 11111111L, "test", 0);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/comment", commentInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("评论成功"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("添加评论-用户不存在")
    void addComment2() {
        CommentInVo commentInVo = new CommentInVo(1L, 11111112L, "test", 0);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/comment", commentInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("用户不存在"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("添加评论-博客不存在")
    void addComment3() {
        CommentInVo commentInVo = new CommentInVo(2L, 11111111L, "test", 0);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/comment", commentInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该博客不存在"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser1.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("添加评论-用户被封")
    void addComment4() {
        CommentInVo commentInVo = new CommentInVo(1L, 11111111L, "test", 0);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/comment", commentInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该用户已被封禁，无法评论"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlogNoComment.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("添加评论-评论功能关闭")
    void addComment5() {
        CommentInVo commentInVo = new CommentInVo(1L, 11111111L, "test", 0);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/comment", commentInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该博客已关闭评论功能"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("删除评论")
    void deleteComment1() {
        Long commentId = 1L;
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/delete/{commentId}", null, String.class, commentId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("删除成功"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("删除评论-评论不存在")
    void deleteComment2() {
        Long commentId = 2L;
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/delete/{commentId}", null, String.class, commentId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该评论不存在！"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("删除评论-没有删除权限")
    void deleteComment3() {
        Long commentId = 1L;
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/comments/blog/delete/{commentId}", null, String.class, commentId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("没有权限删除！"));
    }


    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("获取博客所有评论")
    void getCommentListAll1() {
        Long blogId = 1L;
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/comments/{blogId}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                blogId,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("操作成功"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("获取博客所有评论-该博客不存在")
    void getCommentListAll2() {
        Long blogId = 2L;
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/comments/{blogId}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                blogId,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该博客不存在！"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql"),
            @Sql("/testcase/sql/comment/initComment.sql")
    })
    @DisplayName("获取博客所有评论-分页参数异常")
    void getCommentListAll3() {
        Long blogId = 1L;
        Map<String, Object> params = new HashMap<>();
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/comments/{blogId}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                blogId,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("分页参数异常"));
    }
}