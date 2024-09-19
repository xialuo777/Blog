package com.blog.mapper;

import com.blog.entity.Category;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static com.blog.mapper.AssertHelperSipf.*;
import static org.junit.jupiter.api.Assertions.*;
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryMapperTest {
    private final String DELETE_ALL_SQL = "delete from category";
    private final String QUERY_ALL_SQL = "select * from category";
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void deleteByPrimaryKey() {
        Category category1 = randomT(Category.class);
        categoryMapper.insert(category1);
        Category category = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = categoryMapper.deleteByPrimaryKey(category.getCategoryId());
        assertEquals(1,i);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insert() {
        Category category = randomT(Category.class);
        categoryMapper.insert(category);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(category, dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertSelective() {
        Category category = randomT(Category.class);
        categoryMapper.insert(category);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(category, dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByPrimaryKey() {
        Category category1 = randomT(Category.class);
        categoryMapper.insert(category1);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        Category category = categoryMapper.selectByPrimaryKey(dbRecord.getCategoryId());
        assertBean(dbRecord, category);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKeySelective() {
        Category category1 = randomT(Category.class);
        categoryMapper.insert(category1);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        Category category = randomT(Category.class);
        category.setCategoryId(dbRecord.getCategoryId());
        categoryMapper.updateByPrimaryKeySelective(category);
        Category dbRecord2 = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(category, dbRecord2);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKey() {
        Category category1 = randomT(Category.class);
        categoryMapper.insert(category1);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        Category category = randomT(Category.class);
        category.setCategoryId(dbRecord.getCategoryId());
        categoryMapper.updateByPrimaryKeySelective(category);
        Category dbRecord2 = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(category, dbRecord2);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void increatCategoryRank() {
        Category category1 = randomT(Category.class);
        categoryMapper.insert(category1);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = categoryMapper.increatCategoryRank(dbRecord);
        assertEquals(1,i);
    }
}