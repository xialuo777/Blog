package com.blog.controller;
import com.blog.config.Constant;

import com.blog.util.SnowFlakeUtil;
import com.blog.util.bo.HttpSessionBO;
import com.blog.vo.Register;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;


import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Extensions(@ExtendWith({SpringExtension.class, OutputCaptureExtension.class}))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    @MockBean
    private HttpSessionBO sessionBO;

    @BeforeEach
    void setUp() {
        sessionBO = new HttpSessionBO<>("2436056388@qq.com","tested");
    }


    @Test
    @Sql(statements = "insert into user (user_id, account, nick_name, password, email, phone) " +
            "values ('1231413515','accountTest','SuperMan','passwordTest','2436056388@qq.com','18539246184')",executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testRegister() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        Register register = new Register("accountTest", "SuperMan", "passwordTest", "passwordTest", "2436056388@qq.com", "18539246184", "tested");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(Constant.BASE_URL+"/users/register", register,String.class, sessionBO);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }

}
