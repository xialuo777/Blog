package com.blog.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public class SnowFlakeUtil {
    private static Snowflake snowflake;

    public SnowFlakeUtil(long workerId, long dataCenterId) {
        this.snowflake = IdUtil.createSnowflake(workerId, dataCenterId);
    }

    public static long nextId() {
        return snowflake.nextId();
    }

    public static String nextIdStr() {
        return snowflake.nextIdStr();
    }
    public static SnowFlakeUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }

    // 单例模式获取Snowflake实例的静态方法
    private static class InstanceHolder {
        private static final SnowFlakeUtil INSTANCE = new SnowFlakeUtil(1, 1);
    }


}