package com.blog.authentication;

import com.blog.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements Filter {

    private final JwtProcessor jwtProcessor;
    private final CurrentUserHolder currentUserHolder;
    private final Set<String> ALLOWED_PATHS = new HashSet<>(Arrays.asList(
            "/users/login",
            "/users/refresh",
            "/users/getCode",
            "/users/register",
            "/users/logout",
            "/users/getUsers"
    ));

    @Override
    public void doFilter(@NotNull ServletRequest servletRequest, @NotNull ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //从"Authorization"请求头中获取accessToken
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring("Bearer ".length());
        }

        String requestURI = request.getRequestURI();
        /*登录、注册、令牌刷新等操作不验证身份，对其他的业务操作进行验证*/

        try {
            if (ALLOWED_PATHS.contains(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
            //extractUserId内部进行token验证时会对invalidToken的异常进行处理
            Map<String, Object> userMap = jwtProcessor.extractUserMap(accessToken);
            Long userId = (Long) userMap.get("userId");
            currentUserHolder.setUserId(userId);
            filterChain.doFilter(request, response);
        } finally {
            //注意：当前请求结束后一定要清理线程，不然会有内存泄漏，
            // 下一个请求会服用上一个请求用户Id等风险
            currentUserHolder.clear();
        }

    }




}