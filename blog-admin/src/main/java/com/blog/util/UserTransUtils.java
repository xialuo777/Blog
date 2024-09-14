package com.blog.util;

import com.blog.entity.User;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserTransUtils {
    public static Map<String, Object> getUserMap(User user) {
        if (user == null) {
            log.error("参数user为null");
            throw new BusinessException(ErrorCode.FAIL, "参数user为null");
        }
        Map<String, Object> userMap = new HashMap<>(3);
        userMap.put("id", user.getUserId());
        userMap.put("nickName", user.getNickName());
        userMap.put("account", user.getAccount());
        return userMap;
    }
}
