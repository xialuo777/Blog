package com.blog.mapper;

import cn.hutool.core.bean.BeanUtil;
import com.blog.entity.Blog;
import com.blog.vo.blog.BlogDesc;
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
class BlogMapperTest {
    private final String delete_all_sql = "delete from blog";
    private final String query_all_sql = "select * from blog";
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(statements = delete_all_sql)
    void deleteByPrimaryKey() {
        insert();
        Blog dbRecord = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        int i = blogMapper.deleteByPrimaryKey(dbRecord.getBlogId());
        assertEquals(1, i);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insert() {
        Blog blog = randomT(Blog.class);
        int insert = blogMapper.insert(blog);
        Blog dbRecord = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord, blog);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insertSelective() {
        Blog blog = randomT(Blog.class);
        int insert = blogMapper.insertSelective(blog);
        Blog dbRecord = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord, blog);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectByPrimaryKey() {
        insert();
        Blog dbRecord = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        Blog blog = blogMapper.selectByPrimaryKey(dbRecord.getBlogId());
        assertBean(dbRecord, blog);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKeySelective() {
        insert();
        Blog dbRecord = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        Blog blog = randomT(Blog.class);
        blog.setBlogId(dbRecord.getBlogId());
        int i = blogMapper.updateByPrimaryKeySelective(blog);
        Blog dbRecord2 = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord2, blog);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKey() {
        insert();
        Blog dbRecord = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        Blog blog = randomT(Blog.class);
        blog.setBlogId(dbRecord.getBlogId());
        int i = blogMapper.updateByPrimaryKeySelective(blog);
        Blog dbRecord2 = getDbRecord(Blog.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord2, blog);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectListByUserId() {
        List<Blog> lists = new ArrayList<>();
        Map<Long, Blog> map = new HashMap<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            Blog blog = randomT(Blog.class);
            blog.setUserId(1L);
            blog.setBlogStatus(0);
            lists.add(blog);
            blogMapper.insert(blog);
            map.put(blog.getBlogId(),blog);
        }
        List<Blog> list = blogMapper.selectListByUserId(1L);
        assertEquals(nums, list.size());
        for (Blog blog : list) {
            assertBean(map.get(blog.getBlogId()), blog);
        }
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectListByCategoryId() {
        List<Blog> lists = new ArrayList<>();
        Map<Long, Blog> map = new HashMap<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            Blog blog = randomT(Blog.class);
            blog.setCategoryId(1L);
            blog.setBlogStatus(0);
            lists.add(blog);
            blogMapper.insert(blog);
            map.put(blog.getBlogId(),blog);
        }
        List<Blog> list = blogMapper.selectListByCategoryId(1L);
        assertEquals(nums, list.size());
        for (Blog blog : list) {
            assertBean(map.get(blog.getBlogId()), blog);
        }
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectList() {
        List<Blog> lists = new ArrayList<>();
        Map<Long, BlogDesc> map = new HashMap<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            Blog blog = randomT(Blog.class);
            blog.setBlogStatus(0);
            lists.add(blog);
            blogMapper.insert(blog);
            BlogDesc blogDesc = new BlogDesc();
            BeanUtil.copyProperties(blog,blogDesc);
            map.put(blog.getBlogId(),blogDesc);
        }
        List<BlogDesc> blogDescList = blogMapper.selectList();
        assertEquals(nums, blogDescList.size());
        for (BlogDesc blogDesc : blogDescList) {
            assertBean(map.get(blogDesc.getBlogId()), blogDesc);
        }
    }
}