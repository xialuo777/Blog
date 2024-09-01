package com.blog.util.redis;

public class RedisTransKey {

    private static final String REDIS_NAME_SPACE="user";
    private static final String REDIS_TOKEN_NAME="token";
    private static final String REDIS_REFRESH_TOKEN_NAME="refreshToken";
    private static final String REDIS_LOGIN_NAME="login";
    private static final String REDIS_EMAIL_CODE_NAME="emailCode";

    public static String setEmailKey(String key){
        return REDIS_NAME_SPACE+":"+REDIS_EMAIL_CODE_NAME+":"+key;
    }
    public static String setRootKey(String key){
        return REDIS_NAME_SPACE+":"+key+"";
    }
    public static String setTokenKey(String key){
        return REDIS_NAME_SPACE+':'+REDIS_TOKEN_NAME+":"+key;
    }
    public static String setRefreshTokenKey(String key){
        return REDIS_NAME_SPACE+':'+REDIS_REFRESH_TOKEN_NAME+":"+key;
    }
    public static String setLoginKey(String key){
        return REDIS_NAME_SPACE+':'+REDIS_LOGIN_NAME+":"+key;
    }

    public static String getEmailKey(String key){return setEmailKey(key);}
    public static String getRootKey(String key){return setRootKey(key);}
    public static String getTokenKey(String key){return setTokenKey(key);}
    public static String getRefreshTokenKey(String key){return setRefreshTokenKey(key);}
    public static String getLoginKey(String key){return setLoginKey(key);}

}

