package com.blog.util;

import com.blog.entity.User;

import java.util.HashMap;
import java.util.Map;

public class UserTransUtils {
    public static Map<String, Object> getUserMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", user.getUserId());
        userMap.put("nickName", user.getNickName());
        userMap.put("account", user.getAccount());
        return userMap;
    }
}
