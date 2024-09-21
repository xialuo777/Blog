package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.Admin;
import com.blog.entity.User;
import com.blog.exception.ResponseResult;
import com.blog.util.JwtProcessor;
import com.blog.util.dto.PageResult;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.admin.AdminInVo;
import com.blog.vo.user.UserVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static com.blog.mapper.AssertHelperSipf.randomT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Extensions({@ExtendWith({SpringExtension.class}), @ExtendWith({OutputCaptureExtension.class})})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class AdminControllerTest {
    @MockBean
    private RedisProcessor redisProcessor;
    @MockBean
    private JwtProcessor jwtProcessor;
    @MockBean
    private CurrentUserHolder currentUserHolder;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @Sql("/testcase/sql/admin/initAdmin.sql")
    @DisplayName("登录成功")
    void testLogin1() {
        AdminInVo adminInVo = new AdminInVo();
        adminInVo.setAccount("admin");
        adminInVo.setPassword("passwordTest");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/admin/login", adminInVo, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }

    @Test
    @Sql("/testcase/sql/admin/initAdmin.sql")
    @DisplayName("登录失败-密码错误")
    void testLogin2() {
        AdminInVo adminInVo = new AdminInVo();
        adminInVo.setAccount("admin");
        adminInVo.setPassword("passwordTest1");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/admin/login", adminInVo, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("密码错误"));
    }

    @Test
    @Sql("/testcase/sql/admin/initAdmin.sql")
    @DisplayName("登录失败-账号不存在")
    void testLogin3() {
        Admin admin = new Admin(11111111L, "admin1", "password");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/admin/login", admin, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("该管理员账号不存在"));
    }


    @Test
    @Sql("/testcase/sql/admin/initAdmin.sql")
    @DisplayName("更新管理员账号")
    void testUpdateAdmin1() {
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);
        AdminInVo adminInVo = randomT(AdminInVo.class);
        adminInVo.setAccount("newAdmin");
        adminInVo.setPassword("newPassword");
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/update", adminInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("管理员信息更新成功"));
    }

    @Test
    @Sql("/testcase/sql/admin/initAdmin.sql")
    @DisplayName("更新管理员账号-不存在")
    void testUpdateAdmin2() {
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        when(jwtProcessor.validateToken("token", 11111112L)).thenReturn(true);
        AdminInVo adminInVo = randomT(AdminInVo.class);
        adminInVo.setAccount("newAdmin");
        adminInVo.setPassword("newPassword");
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/update", adminInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql("/testcase/sql/admin/initAdmin.sql")
    @DisplayName("更新管理员账号-token验证失败")
    void testUpdateAdmin3() {
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(false);
        AdminInVo adminInVo = randomT(AdminInVo.class);
        adminInVo.setAccount("newAdmin");
        adminInVo.setPassword("newPassword");
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/update", adminInVo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("token验证失败"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("查看用户信息")
    void getUser() {
        Long userId = 11111111L;
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);
        ResponseEntity<User> response = testRestTemplate.getForEntity("/admin/user/{userId}", User.class, userId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("删除用户")
    void delete1() {
        Long userId = 11111111L;
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/user/delete/{userId}", null, String.class, userId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("用户删除成功"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("删除用户-用户不存在")
    void delete2() {
        Long userId = 11111112L;
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/user/delete/{userId}", null, String.class, userId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("用户不存在"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户状态")
    void updateUserStatus1() {
        Long userId = 11111111L;
        int userStatus = 1;
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/user/status?userId={userId}&status={userStatus}", null, String.class, userId, userStatus);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("用户状态更新成功"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户状态-用户不存在")
    void updateUserStatus2() {
        Long userId = 11111112L;
        int userStatus = 1;
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/user/status?userId={userId}&status={userStatus}", null, String.class, userId, userStatus);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("用户不存在"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("根据昵称查询用户")
    public void testGetUsersByNickName1() {
        String nickName = "user";
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);

        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(
                "/admin/{nickName}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                nickName,
                params.get("pageNo"),
                params.get("pageSize")
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        System.out.println(responseEntity.getBody());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("根据昵称查询用户-不存在")
    public void testGetUsersByNickName2() {
        String nickName = "user1";
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(
                "/admin/{nickName}?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                nickName,
                params.get("pageNo"),
                params.get("pageSize")
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        System.out.println(responseEntity.getBody());
        assertTrue(responseEntity.getBody().contains("未找到用户"));
    }


    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("根据昵称查询用户-分页参数异常")
    public void testGetUsersByNickName3() {
        String nickName = "user";
        ResponseEntity<ResponseResult> response = testRestTemplate.getForEntity(
                "/admin/{nickName}",
                ResponseResult.class,
                nickName
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseResult<PageResult<UserVo>> body = response.getBody();
        assertTrue(body.getMsg().contains("分页参数异常"));

    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("获取用户")
    void testGetUsers1(){
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/admin/getUsers?pageNo=1&pageSize=5", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("获取用户-分页参数异常")
    void testGetUsers2(){
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/admin/getUsers?pageNo=1", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("分页参数异常"));
    }

    @Test
    @Sql("/testcase/sql/blog/initBlog.sql")
    @DisplayName("删除博客")
    void deleteBlog1() {
        Long blogId = 1L;
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/delete/{blogId}", null, String.class, blogId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("删除成功"));
    }
    @Test
    @Sql("/testcase/sql/blog/initBlog.sql")
    @DisplayName("删除博客-博客不存在")
    void deleteBlog2() {
        Long blogId = 2L;
        ResponseEntity<String> response = testRestTemplate.postForEntity("/admin/delete/{blogId}", null, String.class, blogId);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("文章不存在！"));
    }
    @Test
    @Sql("/testcase/sql/blog/initBlog.sql")
    @DisplayName("获取博客列表")
    void getBlogList1() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/admin/blog/list?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }
    @Test
    @Sql("/testcase/sql/blog/initBlog.sql")
    @DisplayName("获取博客列表-分页参数异常")
    void getBlogList2() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageSize", 10);
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/admin/blog/list?pageNo={pageNo}&pageSize={pageSize}",
                String.class,
                params.get("pageNo"),
                params.get("pageSize"));
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("分页参数异常"));
    }
}