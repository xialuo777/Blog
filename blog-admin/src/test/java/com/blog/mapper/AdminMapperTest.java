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
    private final String delete_all_sql = "delete from admin";
    private final String query_all_sql = "select * from admin";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AdminMapper adminMapper;

    @Test
    @Sql(statements = delete_all_sql)
    void deleteByPrimaryKey() {
        insert();
        Admin record = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        int deleteByPrimaryKey = adminMapper.deleteByPrimaryKey(record.getAdminId());
        assertEquals(1, deleteByPrimaryKey);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insert() {
        Admin admin = randomT(Admin.class);
        int insert = adminMapper.insert(admin);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        assertBean(record, admin);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void insertSelective() {

        Admin admin = randomT(Admin.class);
        int insertSelective = adminMapper.insertSelective(admin);
        Admin record = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        assertBean(record, admin);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void selectByPrimaryKey() {
        insert();
        Admin record = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        Admin admin = adminMapper.selectByPrimaryKey(record.getAdminId());
        assertBean(admin, record);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKeySelective() {
        insert();
        Admin record = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        Admin admin = new Admin();
        admin.setAccount("adminUp");
        admin.setPassword(record.getPassword());
        admin.setAdminId(record.getAdminId());
        adminMapper.updateByPrimaryKey(admin);

        Admin record2 = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        assertBean(record2, admin);
    }

    @Test
    @Sql(statements = delete_all_sql)
    void updateByPrimaryKey() {
        insert();
        Admin record = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        Admin admin = new Admin();
        admin.setAccount("adminUp");
        admin.setPassword("12345678");
        admin.setAdminId(record.getAdminId());
        adminMapper.updateByPrimaryKey(admin);

        Admin record2 = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        assertBean(record2, admin);
    }

    @Test
    void selectByAccount() {
        insert();
        Admin record = getDbRecord(Admin.class, jdbcTemplate, query_all_sql);
        Admin admin = adminMapper.selectByAccount(record.getAccount());
        assertBean(admin, record);
    }
}