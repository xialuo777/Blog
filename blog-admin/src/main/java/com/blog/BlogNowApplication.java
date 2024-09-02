package com.blog;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blog.mapper")
@EnableEncryptableProperties
public class BlogNowApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogNowApplication.class, args);
    }

}
