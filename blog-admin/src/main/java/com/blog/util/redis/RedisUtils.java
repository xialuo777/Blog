package com.blog.util.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.concurrent.TimeUnit;
@Component
@RequiredArgsConstructor
public class RedisUtils {
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 注入
     * @param redisTemplate 模板
     */
    @Autowired
    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     *  判断key是否存在
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    /**
     *  删除缓存
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
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
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value  值
     * @return true成功 false失败
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }


    /**
     *  普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
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
     * @return true成功 false 失败
     */

    public void set(String key, Object value, long time,TimeUnit tiemtype) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, tiemtype);
        } else {
            throw new RuntimeException("设置的验证码超时时间小于0");
        }
    }





}
