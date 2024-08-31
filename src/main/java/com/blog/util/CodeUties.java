package com.blog.util;

import java.util.Random;

public class CodeUties {
    private static final Random RANDOM = new Random();
    /**
     * @Title CodeUties
     * @Description 获取6位验证码
     * @return String
     * @Data 2024/8/22
     */
    public static String getCode() {
        int num = 6;
        String code = "";
        for (int i = 0; i < num; i++) {
            int type = RANDOM.nextInt(3);
            switch (type){
                case 0:
                    code += RANDOM.nextInt(10);
                    break;
                case 1:
                    code += (char)('a' + RANDOM.nextInt(26));
                    break;
                default:
                    code += (char)('A' + RANDOM.nextInt(26));
            }
        }
        return code;
    }

}
