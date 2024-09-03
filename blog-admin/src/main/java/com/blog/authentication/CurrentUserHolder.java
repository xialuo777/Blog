package com.blog.authentication;

import org.springframework.stereotype.Component;

@Component
public class CurrentUserHolder {
    private static ThreadLocal<Long> currentUser = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        currentUser.set(userId);
    }

    public static Long getUserId() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}