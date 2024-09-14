package com.blog.util;

import java.util.Random;

/**
 * @author: zhang
 * @time: 2024-09-14 13:01
 */
public class CodeUtils {
    private static final Random RANDOM = new Random();
    /**
     * 获取6位验证码
     * @return String
     * @time 2024/8/22
     */
    public static String getCode() {
        int num = 6;
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int type = RANDOM.nextInt(3);
            switch (type){
                case 0:
                    codeBuilder.append(RANDOM.nextInt(10));
                    break;
                case 1:
                    codeBuilder.append ((char)('a' + RANDOM.nextInt(26)));
                    break;
                default:
                    codeBuilder.append((char)('A' + RANDOM.nextInt(26)));
            }
        }
        return codeBuilder.toString();
    }

}
