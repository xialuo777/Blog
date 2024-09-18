package com.blog.mapper;

import com.blog.entity.Tag;
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
class TagMapperTest {
    private final String delete_all_sql = "delete from tag";
    private final String query_all_sql = "select * from tag";
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    @Sql(statements = delete_all_sql)
    void deleteByPrimaryKey() {
        insert();
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        int i = tagMapper.deleteByPrimaryKey(dbRecord.getTagId());
        assertEquals(1,i);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insert() {
        Tag tag = randomT(Tag.class);
        tagMapper.insert(tag);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord,tag);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insertSelective() {
        Tag tag = randomT(Tag.class);
        tagMapper.insert(tag);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord,tag);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectByPrimaryKey() {
        insert();
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        Tag tag = tagMapper.selectByPrimaryKey(dbRecord.getTagId());
        assertBean(tag,dbRecord);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKeySelective() {
        insert();
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        Tag tag = randomT(Tag.class);
        tag.setTagId(dbRecord.getTagId());
        tagMapper.updateByPrimaryKeySelective(tag);
        Tag dbRecord2 = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord2,tag);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKey() {
        insert();
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        Tag tag = randomT(Tag.class);
        tag.setTagId(dbRecord.getTagId());
        tagMapper.updateByPrimaryKeySelective(tag);
        Tag dbRecord2 = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        assertBean(dbRecord2,tag);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectByTagName() {
        Tag tag1 = randomT(Tag.class);
        tag1.setDeleteFlag(0);
        tagMapper.insert(tag1);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, query_all_sql);
        Tag tag = tagMapper.selectByTagName(dbRecord.getTagName());
        assertBean(tag,dbRecord);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insertList() {
        Map<Long,Tag> map = new HashMap<>();
        List<Tag> list = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        int num = 10;
        for (int i = 0; i < num; i++) {
            Tag tag = randomT(Tag.class);
            tag.setTagName("tag"+i);
            tagMapper.insert(tag);
            map.put(tag.getTagId(),tag);
            list.add(tag);
            stringList.add(tag.getTagName());
        }

        List<Tag> tags = tagMapper.selectListByTagNames(stringList);
        for (Tag tag : tags) {
            assertBean(tag,map.get(tag.getTagId()));
        }
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectListByTagNames() {
        Map<Long,Tag> map = new HashMap<>();
        List<Tag> list = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        int num = 10;
        for (int i = 0; i < num; i++) {
            Tag tag = randomT(Tag.class);
            tag.setTagName("tag"+i);
            tagMapper.insert(tag);
            map.put(tag.getTagId(),tag);
            list.add(tag);
            stringList.add(tag.getTagName());
        }

        List<Tag> tags = tagMapper.selectListByTagNames(stringList);
        for (Tag tag : tags) {
            assertBean(tag,map.get(tag.getTagId()));
        }
    }
}