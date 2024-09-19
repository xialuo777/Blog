package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import com.blog.vo.user.UserInfoVo;
import com.ccb.sunmao.test.AbstractIntTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static com.blog.mapper.AssertHelperSipf.randomT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserControllerTest extends AbstractIntTest {
    @MockBean
    private RedisProcessor redisProcessor;
    @MockBean
    private JwtProcessor jwtProcessor;
    @MockBean
    private CurrentUserHolder currentUserHolder;


    @BeforeEach
    public void init() {
        EmailCodeBo mockEmailCodeBo = new EmailCodeBo();
        mockEmailCodeBo.setCode("tested");
        mockEmailCodeBo.setEmail("2436056388@qq.com");
        when(redisProcessor.get(anyString())).thenReturn(Optional.of(mockEmailCodeBo));

    }

    @Test
    @DisplayName("获取邮箱验证码")
    void getCode1() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/users/getCode?email=2436056388@qq.com", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("验证码发送成功"));
    }

    @Test
    @DisplayName("获取邮箱验证码-邮箱输入有误")
    void getCode() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/users/getCode?email=2436056388", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("邮件发送失败，请检查邮箱是否输入正确"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("用户注册")
    void testRegister() {
        Register register = new Register("accountTest", "SuperMan", "passwordTest", "passwordTest", "2436056388@qq.com", "18539246184", "tested");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/register", register, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("用户登录成功")
    void testLogin1() {
        Loginer loginer = new Loginer("2436056388@qq.com", "passwordTest");
        //userController.logout()
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/login", loginer, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("用户登录-密码错误")
    void testLogin2() {
        Loginer loginer = new Loginer("2436056388@qq.com", "passwordTest1");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/login", loginer, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("密码错误，登陆失败，请重新输入"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("退出登录")
    void testLogout1() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("accessToken");
        when(jwtProcessor.validateToken("accessToken", 11111111L)).thenReturn(true);
        ResponseEntity<String> responseEntity = this.testRestTemplate.getForEntity("/users/logout", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("退出登录-用户不存在")
    void testLogout2() {
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        when(redisProcessor.get(anyString())).thenReturn("accessToken");
        when(jwtProcessor.validateToken("accessToken", 11111112L)).thenReturn(true);
        ResponseEntity<String> responseEntity = this.testRestTemplate.getForEntity("/users/logout", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("用户不存在"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("退出登录-token验证失败")
    void testLogout3() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("accessToken");
        when(jwtProcessor.validateToken("accessToken", 11111111L)).thenReturn(false);
        ResponseEntity<String> responseEntity = this.testRestTemplate.getForEntity("/users/logout", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("token验证失败"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("删除账户")
    void testDelete1() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/delete", null, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("用户删除成功"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("删除账户-用户不存在")
    void testDelete2() {
        when(currentUserHolder.getUserId()).thenReturn(11111112L);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/delete", null, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("用户不存在"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("删除账户-token验证失败")
    void testDelete3() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(false);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/delete", null, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("token验证失败"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户")
    void testUpdate1() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);
        UserInfoVo userInfoVo = randomT(UserInfoVo.class);
        userInfoVo.setUserId(11111111L);
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/update", userInfoVo, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("用户信息更新成功"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户-用户不存在")
    void testUpdate2() {
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        UserInfoVo userInfoVo = randomT(UserInfoVo.class);
        userInfoVo.setUserId(11111112L);
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/update", userInfoVo, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("用户不存在"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户-token验证失败")
    void testUpdate3() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(false);
        UserInfoVo userInfoVo = randomT(UserInfoVo.class);
        userInfoVo.setUserId(11111111L);
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/update", userInfoVo, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("token验证失败"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("用户主页")
    void testGetProfile1() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);

        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/users/home", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("用户主页-用户不存在")
    void testGetProfile2() {
        when(currentUserHolder.getUserId()).thenReturn(11111112L);

        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/users/home", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("用户不存在"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("用户主页-token验证失败")
    void testGetProfile3() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(false);

        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/users/home", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("token验证失败"));
    }

    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("获取用户")
    void testGetUsers1(){
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/users/getUsers?pageNo=1&pageSize=5", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("操作成功"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("获取用户-分页参数异常")
    void testGetUsers2(){
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/users/getUsers?pageNo=1", String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("分页参数异常"));
    }
}
