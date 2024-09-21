package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.util.JwtProcessor;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.blog.BlogInVo;
import com.blog.vo.blog.BlogUpdateVo;
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
class BlogControllerTest {
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
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("新增博客")
    void saveBlog1() {
        BlogInVo blogInVo = new BlogInVo(1L, 11111111L, "url", "thumbnail", "title", "desc", "content", 1, "categoryName", "tag", 0, 0);
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/save", blogInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("文章保存成功"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("新增博客-用户不存在")
    void saveBlog2() {
        BlogInVo blogInVo = new BlogInVo(1L, 11111111L, "url", "thumbnail", "title", "desc", "content", 1, "categoryName", "tag", 0, 0);
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/save", blogInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("用户不存在"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser1.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("新增博客-该用户已被封禁，无法发布文章！")
    void saveBlog3() {
        BlogInVo blogInVo = new BlogInVo(1L, 11111111L, "url", "thumbnail", "title", "desc", "content", 1, "categoryName", "tag", 0, 0);
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/save", blogInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该用户已被封禁，无法发布文章！"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("更新博客")
    void updateBlog1() {
        BlogUpdateVo blogUpdateVo = new BlogUpdateVo(1L, "thumbnail", "title", "desc", "content", 1, "tag", 0, 0);
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/update/{blogId}", blogUpdateVo, String.class, blogUpdateVo.getBlogId());
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("文章更新成功"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("更新博客-文章不存在")
    void updateBlog2() {
        BlogUpdateVo blogUpdateVo = new BlogUpdateVo(2L, "thumbnail", "title", "desc", "content", 1, "tag", 0, 0);
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/update/{blogId}", blogUpdateVo, String.class, blogUpdateVo.getBlogId());
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("文章不存在"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("更新博客-无权限")
    void updateBlog3() {
        BlogUpdateVo blogUpdateVo = new BlogUpdateVo(1L, "thumbnail", "title", "desc", "content", 1, "tag", 0, 0);
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/update/{blogId}", blogUpdateVo, String.class, blogUpdateVo.getBlogId());
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("您没有权限修改该文章"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser1.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("更新博客-用户被封禁")
    void updateBlog4() {
        BlogUpdateVo blogUpdateVo = new BlogUpdateVo(1L, "thumbnail", "title", "desc", "content", 1, "tag", 0, 0);
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/update/{blogId}", blogUpdateVo, String.class, blogUpdateVo.getBlogId());
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该用户已被封禁，无法修改文章！"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("获取用户博客列表")
    void getCurrentUserBlogList1() {
        Map<String, Object> params = new HashMap<>();
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/list?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("操作成功"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("获取用户博客列表-分页参数异常")
    void getCurrentUserBlogList2() {
        Map<String, Object> params = new HashMap<>();
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        params.put("pageNo", 1);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/list?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("分页参数异常"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser1.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("获取用户博客列表-列表为空")
    void getCurrentUserBlogList3() {
        Map<String, Object> params = new HashMap<>();
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/list?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("当前用户博客列表为空"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("根据id获取博客")
    void getBlog1() {
        Long blogId = 1L;
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/detail/{blogId}", String.class, blogId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("操作成功"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("根据id获取博客-博客不存在")
    void getBlog2() {
        Long blogId = 2L;
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/detail/{blogId}", String.class, blogId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("文章不存在"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("根据分类获得博客列表")
    void getBlogListByCategoryId1() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/category/{categoryId}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                1L,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("操作成功"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("根据分类获得博客列表-分页参数异常")
    void getBlogListByCategoryId2() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/category/{categoryId}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                1L,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("分页参数异常"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("根据分类获得博客列表-该分类下暂无文章")
    void getBlogListByCategoryId3() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/category/{categoryId}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                2L,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("该分类下暂无文章"));
    }

    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("删除文章")
    void deleteBlog1() {
        Long blogId = 1L;
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/delete/{blogId}", null, String.class, blogId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("删除成功"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("删除文章-文章不存在")
    void deleteBlog2() {
        Long blogId = 2L;
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/delete/{blogId}", null, String.class, blogId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("文章不存在"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("删除文章-没有权限")
    void deleteBlog3() {
        Long blogId = 1L;
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/blogs/delete/{blogId}", null, String.class, blogId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("您没有权限删除该文章"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("获取所有博客列表")
    void getBlogList1() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/blog/list?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("操作成功"));
    }
    @Test
    @SqlGroup({
            @Sql("/testcase/sql/user/initUser.sql"),
            @Sql("/testcase/sql/blog/initBlog.sql")
    })
    @DisplayName("获取所有博客列表-分页参数异常")
    void getBlogList2() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        ResponseEntity<String> response = testRestTemplate.getForEntity("/blogs/blog/list?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("分页参数异常"));
    }
}