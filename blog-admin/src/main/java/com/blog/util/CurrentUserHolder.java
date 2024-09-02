package com.blog.util;

public class CurrentUserHolder {
    private static final ThreadLocal<Long> currentUser = new ThreadLocal<>();

    public static void setUser(Long userId) {
        currentUser.set(userId);
    }

    public static Long getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}