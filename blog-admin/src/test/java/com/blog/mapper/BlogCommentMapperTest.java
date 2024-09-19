package com.blog.mapper;

import com.blog.entity.Admin;
import com.blog.entity.Blog;
import com.blog.entity.BlogComment;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.*;

import static com.blog.mapper.AssertHelperSipf.*;
import static org.junit.jupiter.api.Assertions.*;
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BlogCommentMapperTest {
    private final String DELETE_ALL_SQL = "delete from blog_comment";
    private final String QUERY_ALL_SQL = "select * from blog_comment";
    @Autowired
    private BlogCommentMapper blogCommentMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void deleteByPrimaryKey() {
        BlogComment blogComment1 = randomT(BlogComment.class);
        blogCommentMapper.insert(blogComment1);
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = blogCommentMapper.deleteByPrimaryKey(blogComment.getCommentId());
        assertEquals(1, i);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insert() {
        BlogComment blogComment = randomT(BlogComment.class);
        int insert = blogCommentMapper.insert(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertSelective() {
        BlogComment blogComment = randomT(BlogComment.class);
        int insert = blogCommentMapper.insertSelective(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByPrimaryKey() {
        BlogComment blogComment1 = randomT(BlogComment.class);
        blogCommentMapper.insert(blogComment1);
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        BlogComment record = blogCommentMapper.selectByPrimaryKey(blogComment.getCommentId());
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKeySelective() {
        BlogComment blogComment1 = randomT(BlogComment.class);
        blogCommentMapper.insert(blogComment1);
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        blogComment.setCommentBody("new comment body");
        int i = blogCommentMapper.updateByPrimaryKeySelective(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKey() {
        BlogComment blogComment1 = randomT(BlogComment.class);
        blogCommentMapper.insert(blogComment1);
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        blogComment.setCommentBody("new comment body");
        blogComment.setCommentator("new commentator");
        int i = blogCommentMapper.updateByPrimaryKey(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByBlogId() {
        List<BlogComment> lists = new ArrayList<>();
        Map<Long, BlogComment> map = new HashMap<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            BlogComment blogComment = randomT(BlogComment.class);
            blogComment.setBlogId(1L);
            lists.add(blogComment);
            blogCommentMapper.insert(blogComment);
            map.put(blogComment.getCommentId(),blogComment);
        }
        List<BlogComment> list = blogCommentMapper.selectByBlogId(1L);
        assertEquals(nums, list.size());
        for (BlogComment blogComment : list) {
            assertBean(map.get(blogComment.getCommentId()), blogComment);
        }
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectCommentCountByBlogId() {
        List<BlogComment> lists = new ArrayList<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            BlogComment blogComment = randomT(BlogComment.class);
            blogComment.setBlogId(1L);
            lists.add(blogComment);
            blogCommentMapper.insert(blogComment);
        }
        assertEquals(nums, blogCommentMapper.selectCommentCountByBlogId(1L));
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void queryFirstCommentList() {
        BlogComment blogComment = new BlogComment(1L, 1L, "admin", 1L, "comment body", new Date(),null, (byte) 0);
        blogCommentMapper.insert(blogComment);
        BlogComment blogCommentDb = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        assertEquals(1, blogCommentMapper.queryFirstCommentList(blogCommentDb.getBlogId()).size());
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void querySecondCommentList() {
        BlogComment blogComment = new BlogComment(1L, 1L, "admin", 1L, "comment body", new Date(),1L, (byte) 0);
        blogCommentMapper.insert(blogComment);
        BlogComment blogCommentDb = getDbRecord(BlogComment.class, jdbcTemplate, QUERY_ALL_SQL);
        assertEquals(1, blogCommentMapper.querySecondCommentList(blogCommentDb.getBlogId()).size());

    }
}