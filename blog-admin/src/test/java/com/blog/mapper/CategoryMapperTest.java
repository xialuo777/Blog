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
    private final String delete_all_sql = "delete from category";
    private final String query_all_sql = "select * from category";
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(statements = delete_all_sql)
    void deleteByPrimaryKey() {
        insert();
        Category category = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        int i = categoryMapper.deleteByPrimaryKey(category.getCategoryId());
        assertEquals(1,i);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insert() {
        Category category = randomT(Category.class);
        categoryMapper.insert(category);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        assertBean(category, dbRecord);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insertSelective() {
        Category category = randomT(Category.class);
        categoryMapper.insert(category);
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        assertBean(category, dbRecord);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectByPrimaryKey() {
        insert();
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        Category category = categoryMapper.selectByPrimaryKey(dbRecord.getCategoryId());
        assertBean(dbRecord, category);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKeySelective() {
        insert();
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        Category category = randomT(Category.class);
        category.setCategoryId(dbRecord.getCategoryId());
        categoryMapper.updateByPrimaryKeySelective(category);
        Category dbRecord2 = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        assertBean(category, dbRecord2);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKey() {
        insert();
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        Category category = randomT(Category.class);
        category.setCategoryId(dbRecord.getCategoryId());
        categoryMapper.updateByPrimaryKeySelective(category);
        Category dbRecord2 = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        assertBean(category, dbRecord2);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void increatCategoryRank() {
        insert();
        Category dbRecord = getDbRecord(Category.class, jdbcTemplate, query_all_sql);
        int i = categoryMapper.increatCategoryRank(dbRecord);
        assertEquals(1,i);
    }
}