package com.blog.authentication;

import com.blog.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements Filter {

    private final RequestAuthentication requestAuthentication;


    @Override
    public void doFilter(@NotNull ServletRequest servletRequest, @NotNull ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        /*登录接口不验证身份，对其他的业务操作进行验证*/
        if (requestURI.equals("/users/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            requestAuthentication.verifyUser();
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }
    }
}