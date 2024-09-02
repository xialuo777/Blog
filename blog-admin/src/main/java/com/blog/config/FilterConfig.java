package com.blog.config;

import com.blog.authentication.AuthenticationFilter;
import com.blog.authentication.RequestAuthentication;
import com.blog.util.JwtProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterFilterRegistration() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthenticationFilter(new RequestAuthentication(new JwtProcessor())));
        registrationBean.addUrlPatterns("/users/*"); // 保护的路径
        return registrationBean;
    }
}