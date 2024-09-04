package com.blog.authentication;

import org.springframework.stereotype.Component;

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