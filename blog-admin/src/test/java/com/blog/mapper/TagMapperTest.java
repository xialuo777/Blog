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
    private final String DELETE_ALL_SQL = "delete from tag";
    private final String QUERY_ALL_SQL = "select * from tag";
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void deleteByPrimaryKey() {
        Tag tag1 = randomT(Tag.class);
        tagMapper.insert(tag1);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = tagMapper.deleteByPrimaryKey(dbRecord.getTagId());
        assertEquals(1,i);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insert() {
        Tag tag = randomT(Tag.class);
        tagMapper.insert(tag);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord,tag);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertSelective() {
        Tag tag = randomT(Tag.class);
        tagMapper.insert(tag);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord,tag);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByPrimaryKey() {
        Tag tag1 = randomT(Tag.class);
        tagMapper.insert(tag1);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        Tag tag = tagMapper.selectByPrimaryKey(dbRecord.getTagId());
        assertBean(tag,dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKeySelective() {
        Tag tag1 = randomT(Tag.class);
        tagMapper.insert(tag1);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        Tag tag = randomT(Tag.class);
        tag.setTagId(dbRecord.getTagId());
        tagMapper.updateByPrimaryKeySelective(tag);
        Tag dbRecord2 = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord2,tag);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKey() {
        Tag tag1 = randomT(Tag.class);
        tagMapper.insert(tag1);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        Tag tag = randomT(Tag.class);
        tag.setTagId(dbRecord.getTagId());
        tagMapper.updateByPrimaryKeySelective(tag);
        Tag dbRecord2 = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(dbRecord2,tag);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByTagName() {
        Tag tag1 = randomT(Tag.class);
        tag1.setDeleteFlag(0);
        tagMapper.insert(tag1);
        Tag dbRecord = getDbRecord(Tag.class, jdbcTemplate, QUERY_ALL_SQL);
        Tag tag = tagMapper.selectByTagName(dbRecord.getTagName());
        assertBean(tag,dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
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
    @Sql(statements = DELETE_ALL_SQL)
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