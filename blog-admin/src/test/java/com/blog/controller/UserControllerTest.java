package com.blog.controller;

import com.blog.constant.Constant;
import com.blog.entity.User;
import com.blog.service.JwtService;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.util.SnowFlakeUtil;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisUtils;
import com.blog.vo.Loginer;
import com.blog.vo.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.port=0")
public class UserControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Mock
    private RedisUtils redisUtils;


    @Test
    @Transactional
    void testRegister() throws Exception {
//        try (MockedStatic<RedisTransKey> redisTransKeyMockedStatic = mockStatic(RedisTransKey.class)) {
        Register register = new Register("accountTest", "SuperMan", "passwordTest", "passwordTest", "2436056388@qq.com", "18539246184", "tested");
        EmailCodeBo emailCodeBo = new EmailCodeBo();
        emailCodeBo.setEmail(register.getEmail());
        emailCodeBo.setCode("tested");

        when(redisUtils.get(RedisTransKey.getEmailKey(register.getEmail()))).thenReturn(emailCodeBo);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/register", register, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // 这里假设生成的 token 是正确的
        String expectedToken = jwtService.generateToken(userService.userRegister(register));
        assertEquals(expectedToken, responseEntity.getBody());
//        }
    }


    @Test
    @Transactional
    void testGetCode() {
        String email = "test@example.com";
        testRestTemplate.getForEntity("/users/email_code?email=" + email, Void.class);
        // Verify that the email code was sent by checking the mail service logic or database
    }

    @Test
    @Transactional
    void testLogin() {
        Loginer loginer = new Loginer("2436056388@qq.com", "passwordTest");
        testRestTemplate.getForEntity("/users/login", Void.class, Collections.singletonMap("email", loginer.getEmail()), Collections.singletonMap("password", loginer.getPassword()));
        // Verify user login by checking session or token
    }

    @Test
    @Transactional
    void testDelete() {
        String email = "2436056388@qq.com";
        testRestTemplate.delete("/users/delete?email=" + email);
        // Verify user deletion by checking the database
    }



}