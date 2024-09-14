package com.blog.config;

import com.blog.authentication.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT过滤器配置
 * @author: zhang
 * @time: 2024-09-14 10:23
 */
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterFilterRegistration(AuthenticationFilter authenticationFilter) {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authenticationFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}