package com.blog.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public class SnowFlakeUtil {
    private static Snowflake snowflake = IdUtil.getSnowflake();
    public static long nextId() {
        return snowflake.nextId();
    }


}