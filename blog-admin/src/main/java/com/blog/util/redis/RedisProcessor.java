package com.blog.util.redis;

import cn.hutool.core.util.ArrayUtil;
import com.blog.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.concurrent.TimeUnit;

/**
 * @author: zhang
 * @time: 2024-09-14 12:59
 */
@Component
@Slf4j
public class RedisProcessor {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     *  删除缓存
     * @param key 可以传一个值 或多个
     */
    public void del(String key) {
        if (ArrayUtil.isNotEmpty(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 普通缓存获取
     * @param key 键
     * @return Object
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value  值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }


    /**
     *  普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time time 时间类型自定义设定
     */

    public void set(String key, Object value, long time,TimeUnit timeType) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, timeType);
        } else {
            throw new BusinessException("设置的保存时间小于0");
        }
    }
}
