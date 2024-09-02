package com.blog.authentication;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.util.CurrentUserHolder;
import com.blog.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestAuthentication {
    private final JwtProcessor jwtProcessor;

    /**
     * 对其他的业务操作进行验证
     */
    public void verifyUser() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String accessToken = requestAttributes.getRequest().getHeader("accessToken");
        Long userId = Long.valueOf(String.valueOf(Optional.ofNullable(requestAttributes.getRequest().getHeader("userId"))));
        if (accessToken == null) {
            log.error("token已过期");
            throw new BusinessException(ErrorCode.TOKEN_ERROR, "token已过期");
        }
        Long tokenUserId = jwtProcessor.extractUserId(accessToken);
        if (!userId.equals(tokenUserId)){
            log.error("非法用户");
            throw new BusinessException(ErrorCode.TOKEN_ERROR,"非法用户");
        }
    }
}
