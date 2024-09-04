package com.blog.authentication;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.util.JwtProcessor;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
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
    private final CurrentUserHolder currentUserHolder;
    private final RedisProcessor redisProcessor;
    private final Set<String> ALLOWED_PATHS = new HashSet<>(Arrays.asList(
            "/users/login",
            "/users/refresh",
            "/users/email_code",
            "/users/register",
            "/users/logout"
    ));

    @Override
    public void doFilter(@NotNull ServletRequest servletRequest, @NotNull ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String accessToken = request.getHeader("accessToken");

        String requestURI = request.getRequestURI();
        /*登录、注册、令牌刷新等操作不验证身份，对其他的业务操作进行验证*/

        try {
            if (ALLOWED_PATHS.contains(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
            //extractUserId内部进行token验证时会对invalidToken的异常进行处理
            Long userId = jwtProcessor.extractUserId(accessToken);
            currentUserHolder.setUserId(userId);
            if (userId == null) {
                log.error("非法用户");
                throw new BusinessException(ErrorCode.TOKEN_ERROR, "非法用户");
            }
            filterChain.doFilter(request, response);
        } finally {
            //注意：当前请求结束后一定要清理线程，不然会有内存泄漏，
            // 下一个请求会服用上一个请求用户Id等风险
            currentUserHolder.clear();
        }

    }




}