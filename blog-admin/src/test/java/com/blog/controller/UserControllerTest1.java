package com.blog.controller;
import com.blog.constant.Constant;

import com.blog.vo.user.Register;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;


import org.springframework.http.*;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Extensions(@ExtendWith({SpringExtension.class, OutputCaptureExtension.class}))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest1 {

    @Test
    void getCode() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(Constant.BASE_URL+"/users/email_code?email=2436056388@qq.com",String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @Sql(statements = "insert into user (user_id, account, nick_name, password, email, phone) " +
            "values ('1231413515','accountTest','SuperMan','passwordTest','2436056388@qq.com','18539246184')",executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Transactional
    void testRegister() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        Register register = new Register("accountTest", "SuperMan", "passwordTest", "passwordTest", "2436056388@qq.com", "18539246184", "tested");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(Constant.BASE_URL+"/users/register", register,String.class);
        System.out.println(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }

}
