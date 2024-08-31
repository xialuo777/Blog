package com.blog.util.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
@Component
public class RedisUtils {
    @Autowired
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
     *  指定缓存失效时间
     * @param key 键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        if (time > 0) {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        } else {
            throw new RuntimeException("超时时间小于0");
        }
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
     * 根据key 获取过期时间
     * @param key 键 不能为null
     * @param tiemtype 时间类型
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key,TimeUnit tiemtype) {

        return redisTemplate.getExpire(key, tiemtype);
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
    public boolean set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        return true;
    }


    /**
     *  普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("设置的验证码超时时间小于0");
        }
        return true;
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


    /**
     * 测试与 Redis 服务器的连接是否活跃。
     * @return true 如果连接是活跃的，否则返回 false。
     */
    public boolean testConnection() {
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                // 使用 ping 命令来测试连接
                return Boolean.valueOf(connection.ping());
            });
        } catch (Exception e) {
            // 如果执行过程中出现异常，则认为连接不活跃
            return false;
        }
    }



}
