package com.blog.mapper;

import com.blog.entity.Blog;
import com.blog.entity.BlogComment;
import com.blog.entity.User;
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
class UserMapperTest {
    private final String DELETE_ALL_SQL = "delete from user";
    private final String QUERY_ALL_SQL = "select * from user";
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void deleteByPrimaryKey() {
        User user1 = randomT(User.class);
        userMapper.insert(user1);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = userMapper.deleteByPrimaryKey(dbRecord.getUserId());
        assertEquals(1,i);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insert() {
        User user = randomT(User.class);
        userMapper.insert(user);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(user, dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertSelective() {
        User user = randomT(User.class);
        userMapper.insert(user);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(user, dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByPrimaryKey() {
        User user1 = randomT(User.class);
        userMapper.insert(user1);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        User user = userMapper.selectByPrimaryKey(dbRecord.getUserId());
        assertBean(user, dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKeySelective() {
        User user1 = randomT(User.class);
        userMapper.insert(user1);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        User user = randomT(User.class);
        user.setUserId(dbRecord.getUserId());
        userMapper.updateByPrimaryKeySelective(user);
        User dbRecord2 = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(user, dbRecord2);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKey() {
        User user1 = randomT(User.class);
        userMapper.insert(user1);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        User user = randomT(User.class);
        user.setUserId(dbRecord.getUserId());
        userMapper.updateByPrimaryKeySelective(user);
        User dbRecord2 = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(user, dbRecord2);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertUser() {
        User user = randomT(User.class);
        user.setDescription(null);
        user.setStatus(0);
        user.setWebsite(null);
        userMapper.insertUser(user);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(user, dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void deleteByEmail() {
        User user1 = randomT(User.class);
        userMapper.insert(user1);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        int i = userMapper.deleteByEmail(dbRecord.getEmail());
        assertEquals(1, i);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void findByEmail() {
        User user1 = randomT(User.class);
        userMapper.insert(user1);
        User dbRecord = getDbRecord(User.class, jdbcTemplate, QUERY_ALL_SQL);
        User user = userMapper.findByEmail(dbRecord.getEmail());
        assertBean(user, dbRecord);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectUsersByNickName() {
        List<User> lists = new ArrayList<>();
        Map<Long, User> map = new HashMap<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            User user = randomT(User.class);
            user.setNickName("SuperMan");
            userMapper.insert(user);
            lists.add(user);
            map.put(user.getUserId(), user);
        }
        List<User> list = userMapper.selectUsersByNickName("SuperMan");
        assertEquals(nums, list.size());
        for (User user : list) {
            assertBean(map.get(user.getUserId()), user);
        }
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectUsers() {
        List<User> lists = new ArrayList<>();
        Map<Long, User> map = new HashMap<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            User user = randomT(User.class);
            userMapper.insert(user);
            lists.add(user);
            map.put(user.getUserId(), user);
        }
        List<User> list = userMapper.selectUsers();
        assertEquals(nums, list.size());
        for (User user : list) {
            assertBean(map.get(user.getUserId()), user);
        }
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectTotalCount() {
        List<User> lists = new ArrayList<>();
        int nums = 10;
        for (int i = 0; i < nums; i++) {
            User user = randomT(User.class);
            userMapper.insert(user);
            lists.add(user);
        }
        int count = userMapper.selectTotalCount();
        assertEquals(nums, count);
    }

}