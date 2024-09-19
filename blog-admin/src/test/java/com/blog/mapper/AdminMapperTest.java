package com.blog.mapper;

import com.blog.entity.Admin;
import lombok.AllArgsConstructor;
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
class AdminMapperTest {
    private final String DELETE_ALL_SQL = "delete from admin";
    private final String QUERY_ALL_SQL = "select * from admin";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AdminMapper adminMapper;

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void deleteByPrimaryKey() {
        Admin admin = randomT(Admin.class);
        adminMapper.insert(admin);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        int deleteByPrimaryKey = adminMapper.deleteByPrimaryKey(record.getAdminId());
        assertEquals(1, deleteByPrimaryKey);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insert() {
        Admin admin = randomT(Admin.class);
        int insert = adminMapper.insert(admin);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record, admin);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void insertSelective() {
        Admin admin = randomT(Admin.class);
        int insertSelective = adminMapper.insertSelective(admin);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record, admin);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void selectByPrimaryKey() {
        Admin admin = randomT(Admin.class);
        adminMapper.insert(admin);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        Admin admin1 = adminMapper.selectByPrimaryKey(record.getAdminId());
        assertBean(admin1, record);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKeySelective() {
        Admin admin1 = randomT(Admin.class);
        adminMapper.insert(admin1);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        Admin admin = new Admin();
        admin.setAccount("adminUp");
        admin.setPassword(record.getPassword());
        admin.setAdminId(record.getAdminId());
        adminMapper.updateByPrimaryKey(admin);

        Admin record2 = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record2, admin);
    }

    @Test
    @Sql(statements = DELETE_ALL_SQL)
    void updateByPrimaryKey() {
        Admin admin1 = randomT(Admin.class);
        adminMapper.insert(admin1);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        Admin admin = new Admin();
        admin.setAccount("adminUp");
        admin.setPassword("12345678");
        admin.setAdminId(record.getAdminId());
        adminMapper.updateByPrimaryKey(admin);

        Admin record2 = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        assertBean(record2, admin);
    }

    @Test
    void selectByAccount() {
        Admin admin1 = randomT(Admin.class);
        adminMapper.insert(admin1);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, QUERY_ALL_SQL);
        Admin admin = adminMapper.selectByAccount(record.getAccount());
        assertBean(admin, record);
    }
}