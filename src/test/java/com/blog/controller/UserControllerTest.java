package com.blog.controller;
import com.blog.config.Constant;
import com.blog.service.MailService;
import com.blog.service.UserService;

import com.blog.util.bo.HttpSessionBO;
import com.blog.vo.Register;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;


import static org.junit.jupiter.api.Assertions.assertEquals;

//@WebMvcTest(UserController.class)
@AutoConfigureMybatis
@RunWith(SpringRunner.class)
@Import({MailService.class, UserService.class})
class UserControllerTest {
/*
    @Autowired
    private HttpSessionBO sessionBO;
    @BeforeEach
    void setUp() {
        sessionBO = new HttpSessionBO<>("2436056388@qq.com","tested");
    }
*/

    @Test
    @Sql(statements = "insert into users (user_id, account, nick_name, password, email, phone) " +
            "values ('123414131L','accountTest','SuperMan','passwordTest','2436056388@qq.com','18539246184')",executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testRegister() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();

        Register register = new Register("accountTest", "SuperMan", "passwordTest", "passwordTest", "2436056388@qq.com", "18539246184", "tested");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(Constant.BASE_URL+"/users/register", register, String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());


    }

}
