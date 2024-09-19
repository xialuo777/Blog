package com.blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityUtils {
    /**
     * 生成BCryptPasswordEncoder密码
     * @param password
     * @return encode password
     */
    public static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public static String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 登陆时判断输入密码和数据库中存储加密后的密码是否一致
     * @param password
     * @param encode
     * @return ture or false
     */
    public static boolean checkPassword(String password, String encode) {
        return passwordEncoder.matches(password, encode);
    }






}


