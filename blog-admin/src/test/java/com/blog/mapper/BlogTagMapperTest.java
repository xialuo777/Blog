package com.blog.mapper;

import com.blog.entity.BlogTag;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.blog.mapper.AssertHelperSipf.*;
import static org.junit.jupiter.api.Assertions.*;
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BlogTagMapperTest {
    private final String DELETE_ALL_SQL = "delete from blog_tag";
    private final String QUERY_ALL_SQL = "select * from blog_tag";
    @Autowired
    private BlogTagMapper blogTagMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void deleteByPrimaryKey() {
        BlogTag blogTag1 = randomT(BlogTag.class);
        blogTagMapper.insert(blogTag1);
        BlogTag blogTag = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = blogTagMapper.deleteByPrimaryKey(blogTag.getId());
        assertEquals(1, i);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insert() {
        BlogTag blogTag = randomT(BlogTag.class);
        int insert = blogTagMapper.insert(blogTag);
        BlogTag dbRecord = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord, blogTag);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertSelective() {
        BlogTag blogTag = randomT(BlogTag.class);
        int insert = blogTagMapper.insertSelective(blogTag);
        BlogTag dbRecord = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord, blogTag);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByPrimaryKey() {
        BlogTag blogTag1 = randomT(BlogTag.class);
        blogTagMapper.insert(blogTag1);
        BlogTag blogTag = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        BlogTag record = blogTagMapper.selectByPrimaryKey(blogTag.getId());
        assertBean(blogTag, record);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKeySelective() {
        BlogTag blogTag1 = randomT(BlogTag.class);
        blogTagMapper.insert(blogTag1);
        BlogTag record = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        BlogTag blogTag = randomT(BlogTag.class);
        blogTag.setId(record.getId());
        blogTagMapper.updateByPrimaryKeySelective(blogTag);
        BlogTag dbRecord = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord, blogTag);
    }

    @Test
    void updateByPrimaryKey() {
        BlogTag blogTag1 = randomT(BlogTag.class);
        blogTagMapper.insert(blogTag1);
        BlogTag record = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        BlogTag blogTag = randomT(BlogTag.class);
        blogTag.setId(record.getId());
        blogTagMapper.updateByPrimaryKeySelective(blogTag);
        BlogTag dbRecord = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord, blogTag);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertList() {
        Map<Long,BlogTag> map = new HashMap<>();
        List<BlogTag> list = new ArrayList<>();
        int num = 10;
        for (int i = 0; i < num; i++) {
            BlogTag blogTag = randomT(BlogTag.class);
            blogTag.setBlogId(1L);
            map.put(blogTag.getId(),blogTag);
            list.add(blogTag);
        }
        int i = blogTagMapper.insertList(list);
        List<BlogTag> blogTags = blogTagMapper.selectListByBlogId(1L);
        for (BlogTag blogTag : blogTags) {
            assertBean(map.get(blogTag.getId()), blogTag);
        }
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectListByBlogId() {
        Map<Long,BlogTag> map = new HashMap<>();
        List<BlogTag> list = new ArrayList<>();
        int num = 10;
        for (int i = 0; i < num; i++) {
            BlogTag blogTag = randomT(BlogTag.class);
            blogTag.setBlogId(1L);
            map.put(blogTag.getId(),blogTag);
            list.add(blogTag);
        }
        int i = blogTagMapper.insertList(list);
        List<BlogTag> blogTags = blogTagMapper.selectListByBlogId(1L);
        for (BlogTag blogTag : blogTags) {
            assertBean(map.get(blogTag.getId()), blogTag);
        }
    }

    @Test
    void deleteByBlogId() {
        BlogTag blogTag = randomT(BlogTag.class);
        blogTag.setBlogId(1L);
        blogTagMapper.insert(blogTag);
        BlogTag record = getDbRecord(BlogTag.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = blogTagMapper.deleteByBlogId(record.getBlogId());
        assertEquals(1,i);
    }
}