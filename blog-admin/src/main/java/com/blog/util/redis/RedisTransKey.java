package com.blog.util.redis;

/**
 * @author: zhang
 * @time: 2024-09-14 13:01
 */
public class RedisTransKey {

    private static final String REDIS_NAME_SPACE="user";
    private static final String REDIS_TOKEN_NAME="token";
    private static final String REDIS_REFRESH_TOKEN_NAME="refreshToken";
    private static final String REDIS_LOGIN_NAME="login";
    private static final String REDIS_EMAIL_CODE_NAME="emailCode";

    public static String emailKey(String key){
        return REDIS_NAME_SPACE+":"+REDIS_EMAIL_CODE_NAME+":"+key;
    }
    public static String tokenKey(String key){
        return REDIS_NAME_SPACE+':'+REDIS_TOKEN_NAME+":"+key;
    }
    public static String refreshTokenKey(String key){
        return REDIS_NAME_SPACE+':'+REDIS_REFRESH_TOKEN_NAME+":"+key;
    }
    public static String loginKey(String key){
        return REDIS_NAME_SPACE+':'+REDIS_LOGIN_NAME+":"+key;
    }

    public static String getEmailKey(String key){return emailKey(key);}
    public static String getTokenKey(String key){return tokenKey(key);}
    public static String getRefreshTokenKey(String key){return refreshTokenKey(key);}
    public static String getLoginKey(String key){return loginKey(key);}

}

