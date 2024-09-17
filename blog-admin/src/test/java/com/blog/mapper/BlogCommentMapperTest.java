package com.blog.mapper;

import com.blog.entity.Admin;
import com.blog.entity.BlogComment;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;
import java.util.List;

import static com.blog.mapper.AssertHelperSipf.*;
import static org.junit.jupiter.api.Assertions.*;
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BlogCommentMapperTest {
    private final String delete_all_sql = "delete from blog_comment";
    private final String query_all_sql = "select * from blog_comment";
    @Autowired
    private BlogCommentMapper blogCommentMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @Sql(statements = delete_all_sql)
    void deleteByPrimaryKey() {
        insert();
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        int i = blogCommentMapper.deleteByPrimaryKey(blogComment.getCommentId());
        assertEquals(1, i);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insert() {
        BlogComment blogComment = randomT(BlogComment.class);
        int insert = blogCommentMapper.insertSelective(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insertSelective() {
        BlogComment blogComment = randomT(BlogComment.class);
        int insert = blogCommentMapper.insertSelective(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectByPrimaryKey() {
        insert();
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        BlogComment record = blogCommentMapper.selectByPrimaryKey(blogComment.getCommentId());
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKeySelective() {
        insert();
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        blogComment.setCommentBody("new comment body");
        int i = blogCommentMapper.updateByPrimaryKeySelective(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKey() {
        insert();
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        blogComment.setCommentBody("new comment body");
        blogComment.setCommentator("new commentator");
        int i = blogCommentMapper.updateByPrimaryKey(blogComment);
        BlogComment record = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        assertBean(record, blogComment);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectByBlogId() {
        insert();
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        List<BlogComment> blogComments = blogCommentMapper.selectByBlogId(blogComment.getBlogId());
        blogComments.forEach(item -> assertBean(item, blogComment));
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectCommentCountByBlogId() {
        insert();
        BlogComment blogComment = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        assertEquals(1, blogCommentMapper.selectCommentCountByBlogId(blogComment.getBlogId()));
    }

    @Test
    @Sql(statements = delete_all_sql)
    void queryFirstCommentList() {
        BlogComment blogComment = new BlogComment(1L, 1L, "admin", 1L, "comment body", new Date(),null, (byte) 0);
        blogCommentMapper.insert(blogComment);
        BlogComment blogCommentDb = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        assertEquals(1, blogCommentMapper.queryFirstCommentList(blogCommentDb.getBlogId()).size());
    }

    @Test
    @Sql(statements = delete_all_sql)
    void querySecondCommentList() {
        BlogComment blogComment = new BlogComment(1L, 1L, "admin", 1L, "comment body", new Date(),1L, (byte) 0);
        blogCommentMapper.insert(blogComment);
        BlogComment blogCommentDb = getDbRecord(BlogComment.class, jdbcTemplate, query_all_sql);
        assertEquals(1, blogCommentMapper.querySecondCommentList(blogCommentDb.getBlogId()).size());

    }
}