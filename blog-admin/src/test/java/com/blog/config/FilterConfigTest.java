package com.blog.config;

import com.blog.authentication.AuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class FilterConfigTest {

    @Mock
    private AuthenticationFilter authenticationFilter;

    @InjectMocks
    private FilterConfig filterConfig;


    @Test
    public void authenticationFilterFilterRegistration() {
        FilterRegistrationBean<AuthenticationFilter> expectedRegistrationBean = new FilterRegistrationBean<>();
        expectedRegistrationBean.setFilter(authenticationFilter);
        expectedRegistrationBean.addUrlPatterns("/*");

        FilterRegistrationBean<AuthenticationFilter> actualRegistrationBean = filterConfig.authenticationFilterFilterRegistration(authenticationFilter);

        assertNotNull(actualRegistrationBean);
        assertEquals(authenticationFilter, actualRegistrationBean.getFilter());
        assertNotNull(actualRegistrationBean.getUrlPatterns());
        assertEquals("/*", actualRegistrationBean.getUrlPatterns().iterator().next());
    }
}
