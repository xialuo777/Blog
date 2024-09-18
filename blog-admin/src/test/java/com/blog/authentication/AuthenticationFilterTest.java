package com.blog.authentication;

import java.util.*;
import java.math.*;

import com.blog.constant.Constant;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.util.JwtProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JwtProcessor mockJwtProcessor;

    @Mock
    private CurrentUserHolder mockCurrentUserHolder;

    @InjectMocks
    private AuthenticationFilter authenticationFilterUnderTest;

    private Set<String> allowedPaths;

    @BeforeEach
    void setUp() {
        allowedPaths = new HashSet<>(Arrays.asList(
                "/users/login",
                "/users/refresh",
                "/users/getCode",
                "/users/register",
                "/users/logout",
                "/admin/login"
        ));
    }

    @Test
    void doFilter_AllowedPath_FilterChainInvoked() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        String requestUri = "/users/login";
        request.setRequestURI(requestUri);

        doNothing().when(filterChain).doFilter(request, response);

        authenticationFilterUnderTest.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(mockCurrentUserHolder).clear();
    }

    @Test
    void doFilter_ValidToken_UserAuthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        String accessToken = "validAccessToken";
        String requestUri = "/some/protected/path";
        Long userId = 1L;

        request.addHeader(Constant.AUTHORIZATION, accessToken);
        request.setRequestURI(requestUri);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put(Constant.ID, userId);

        when(mockJwtProcessor.extractUserMap(accessToken)).thenReturn(userMap);

        doNothing().when(filterChain).doFilter(request, response);
        doNothing().when(mockCurrentUserHolder).setUserId(userId);
        doNothing().when(mockCurrentUserHolder).clear();

        authenticationFilterUnderTest.doFilter(request, response, filterChain);

        verify(mockCurrentUserHolder).setUserId(userId);
        verify(mockCurrentUserHolder).clear();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_InvalidToken_ExceptionHandled(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        String accessToken = "invalidAccessToken";
        String requestUri = "/some/protected/path";

        request.addHeader(Constant.AUTHORIZATION, accessToken);
        request.setRequestURI(requestUri);

        when(mockJwtProcessor.extractUserMap(accessToken)).thenThrow(new BusinessException(ErrorCode.TOKEN_ERROR,"非法的令牌格式"));

        doNothing().when(mockCurrentUserHolder).clear();

        BusinessException businessException = assertThrows(BusinessException.class, () -> authenticationFilterUnderTest.doFilter(request, response, filterChain));
        assertTrue(businessException.getMessage().contains("非法的令牌格式"));
        verify(mockCurrentUserHolder).clear();
    }

    @Test
    void doFilter_ExpiredToken_ExceptionHandled(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        String accessToken = "expiredAccessToken";
        String requestUri = "/some/protected/path";

        request.addHeader(Constant.AUTHORIZATION, accessToken);
        request.setRequestURI(requestUri);

        when(mockJwtProcessor.extractUserMap(accessToken)).thenThrow(new BusinessException(ErrorCode.TOKEN_EXPIRED,"令牌已过期"));

        doNothing().when(mockCurrentUserHolder).clear();

        BusinessException businessException = assertThrows(BusinessException.class, () -> authenticationFilterUnderTest.doFilter(request, response, filterChain));
        assertTrue(businessException.getMessage().contains("令牌已过期"));
        verify(mockCurrentUserHolder).clear();
    }

}
