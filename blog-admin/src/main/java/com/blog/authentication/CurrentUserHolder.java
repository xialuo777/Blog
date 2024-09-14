package com.blog.authentication;

import org.springframework.stereotype.Component;

/**
 * Threadlocal线程变量，存储当前用户id
 * @author: zhang
 * @time: 2024-09-14 10:21
 */
@Component
public class CurrentUserHolder {
    private final ThreadLocal<Long> currentUser = new ThreadLocal<>();

    public void setUserId(Long userId) {
        currentUser.set(userId);
    }

    public Long getUserId() {
        return currentUser.get();
    }

    public void clear() {
        currentUser.remove();
    }
}