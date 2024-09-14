package com.blog.util.redis;

import cn.hutool.core.util.ArrayUtil;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RedisProcessor {
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 注入
     * @param redisTemplate 模板
     */
    @Autowired
    public RedisProcessor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     *  删除缓存
     * @param key 可以传一个值 或多个
     */
    public void del(String... key) {
        if (ArrayUtil.isNotEmpty(key)) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key).toString());
            }
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
     * @param time time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     */
    public void set(String key, Object value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("设置的验证码超时时间小于0");
        }
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
            throw new RuntimeException("设置的验证码超时时间小于0");
        }
    }





}
