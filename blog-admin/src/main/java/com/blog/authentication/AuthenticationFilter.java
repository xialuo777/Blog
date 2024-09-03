package com.blog.authentication;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
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
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements Filter {
    private final JwtProcessor jwtProcessor;


    @Override
    public void doFilter(@NotNull ServletRequest servletRequest, @NotNull ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String accessToken = request.getHeader("accessToken");
        String requestURI = request.getRequestURI();
        /*登录、注册、令牌刷新等操作不验证身份，对其他的业务操作进行验证*/
        Set<String> allowedPaths = new HashSet<>(Arrays.asList(
                "/users/login",
                "/users/refresh",
                "/users/email_code",
                "/users/register",
                "/users/logout"
        ));

        try {
            if (allowedPaths.contains(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
            Long userId = extractUserIdFromToken(accessToken);
            //如果从当前token中取得的用户id为空或与当前线程中存放的userId不一致，则判其为非法用户
            CurrentUserHolder.setUserId(userId);
            if (userId == null) {
                log.error("非法用户");
                throw new BusinessException(ErrorCode.TOKEN_ERROR, "非法用户");
            }
            filterChain.doFilter(request, response);
        } finally {
            //注意：当前请求结束后一定要清理线程，不然会有内存泄漏，
            // 下一个请求会服用上一个请求用户Id等风险
            clear();
        }

    }

    public Long getCurrentUserId() {
        return CurrentUserHolder.getUserId();
    }

    private Long extractUserIdFromToken(String accessToken) {
        try {
            return jwtProcessor.extractUserId(accessToken);
        } catch (Exception e) {
            log.error("Error extracting user ID from token", e);
            return null;
        }
    }

    // 清除当前用户信息
    public void clear() {
        CurrentUserHolder.clear();
    }
}