package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.constant.Constant;
import com.blog.exception.ResponseResult;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.bo.PasswordBo;
import com.blog.util.dto.PageResult;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import com.blog.vo.user.UserInfoVo;
import com.blog.vo.user.UserVo;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static com.blog.mapper.AssertHelperSipf.randomT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@Extensions({@ExtendWith({SpringExtension.class}), @ExtendWith({OutputCaptureExtension.class})})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class UserControllerTest {
    @MockBean
    private RedisProcessor redisProcessor;
    @MockBean
    private JwtProcessor jwtProcessor;
    @MockBean
    private CurrentUserHolder currentUserHolder;
    @Autowired
    private TestRestTemplate testRestTemplate;

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
    @DisplayName("根据昵称查询用户")
    public void testGetUsersByNickName1() {
        String nickName = "user";
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);

        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(
                "/users/{nickName}?pageNo={pageNo}&pageSize={pageSize}",
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
                "/users/{nickName}?pageNo={pageNo}&pageSize={pageSize}",
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
                "/users/{nickName}",
                ResponseResult.class,
                nickName
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseResult<PageResult<UserVo>> body = response.getBody();
        assertTrue(body.getMsg().contains("分页参数异常"));

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
    @DisplayName("更新用户密码")
    public void testUpdatePasswordSuccess1() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);

        PasswordBo passwordBo = new PasswordBo();
        passwordBo.setOldPassword("passwordTest");
        passwordBo.setNewPassword("newPasswordTest");

        ResponseEntity<String> response = testRestTemplate.postForEntity("/users/update/password", passwordBo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("密码修改成功"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("刷新令牌")
    public void testRefreshToken1() {
        String refreshToken = "refreshToken";
        Map<String,Object> userMap = new HashMap<>();
        userMap.put(Constant.USER_MAP_KEY_ID, 11111111L);
        userMap.put(Constant.USER_MAP_KEY_NICK_NAME, "user");
        userMap.put(Constant.USER_MAP_KEY_ACCOUNT, "user");
        when(jwtProcessor.extractUserMap(anyString())).thenReturn(userMap);
        when(jwtProcessor.validateToken(refreshToken,11111111L)).thenReturn(true);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/users/refresh", refreshToken, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());
        assertTrue(response.getBody().contains("操作成功"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser1.sql")
    @DisplayName("刷新令牌-用户状态异常")
    public void testRefreshToken2() {
        String refreshToken = "refreshToken";
        Map<String,Object> userMap = new HashMap<>();
        userMap.put(Constant.USER_MAP_KEY_ID, 11111111L);
        userMap.put(Constant.USER_MAP_KEY_NICK_NAME, "user");
        userMap.put(Constant.USER_MAP_KEY_ACCOUNT, "user");
        when(jwtProcessor.extractUserMap(anyString())).thenReturn(userMap);
        when(jwtProcessor.validateToken(refreshToken,11111111L)).thenReturn(true);
        ResponseEntity<String> response = testRestTemplate.postForEntity("/users/refresh", refreshToken, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());
        assertTrue(response.getBody().contains("该用户处于异常状态，无法执行下一步操作"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户密码-输入密码为空")
    public void testUpdatePasswordSuccess2() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);

        PasswordBo passwordBo = new PasswordBo();
        passwordBo.setOldPassword("passwordTest");
        passwordBo.setNewPassword("");

        ResponseEntity<String> response = testRestTemplate.postForEntity("/users/update/password", passwordBo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("输入密码不能为空"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户密码-用户不存在")
    public void testUpdatePasswordSuccess3() {
        when(currentUserHolder.getUserId()).thenReturn(11111112L);
        PasswordBo passwordBo = new PasswordBo();
        passwordBo.setOldPassword("passwordTest");
        passwordBo.setNewPassword("newPasswordTest");

        ResponseEntity<String> response = testRestTemplate.postForEntity("/users/update/password", passwordBo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("用户不存在！"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户密码-token验证失败")
    public void testUpdatePasswordSuccess4() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(false);

        PasswordBo passwordBo = new PasswordBo();
        passwordBo.setOldPassword("passwordTest");
        passwordBo.setNewPassword("newPasswordTest");

        ResponseEntity<String> response = testRestTemplate.postForEntity("/users/update/password", passwordBo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("token验证失败"));
    }
    @Test
    @Sql("/testcase/sql/user/initUser.sql")
    @DisplayName("更新用户密码-密码验证失败")
    public void testUpdatePasswordSuccess5() {
        when(currentUserHolder.getUserId()).thenReturn(11111111L);
        when(redisProcessor.get(anyString())).thenReturn("token");
        when(jwtProcessor.validateToken("token", 11111111L)).thenReturn(true);

        PasswordBo passwordBo = new PasswordBo();
        passwordBo.setOldPassword("passwordTest1");
        passwordBo.setNewPassword("newPasswordTest");

        ResponseEntity<String> response = testRestTemplate.postForEntity("/users/update/password", passwordBo, String.class);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("密码验证失败，请重新输入"));
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
