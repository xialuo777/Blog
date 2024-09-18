package com.blog.util.redis;

import com.blog.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class RedisProcessorTest {

    @Autowired
    private RedisProcessor redisProcessorUnderTest;


    @Test
    void testDel() {
        redisProcessorUnderTest.set("key", "value");
        assertNotNull(redisProcessorUnderTest.get("key"));
        redisProcessorUnderTest.del("key");
        assertNull(redisProcessorUnderTest.get("key"));
    }

    @Test
    void testGet_KeyIsNull() {
        Object result = redisProcessorUnderTest.get(null);
        assertNull(result, "The result should be null when the key is null.");
    }

    @Test
    public void testGet_ExistingKey() {
        redisProcessorUnderTest.set("key", "value");
        assertEquals("value", redisProcessorUnderTest.get("key"));
    }

    @Test
    public void testGet_NonExistingKey() {
        assertNull(redisProcessorUnderTest.get("nonExistingKey"));
    }

    @Test
    void testSet1() {
        String key = "testKey";
        Object value = "testValue";

        redisProcessorUnderTest.set(key, value);
        assertEquals(value, redisProcessorUnderTest.get(key));
    }
    @Test
    void testSet2() {
        String key = "testKey";
        Object value = "testValue";
        Long time = 7L;
        TimeUnit timeType = TimeUnit.DAYS;

        redisProcessorUnderTest.set(key, value, time, timeType);
        assertEquals(value, redisProcessorUnderTest.get(key));
    }
    @Test
    void testSet_ThrownException() {
        String key = "testKey";
        Object value = "testValue";
        Long time = -1L;
        TimeUnit timeType = TimeUnit.DAYS;

        BusinessException businessException = assertThrows(BusinessException.class, () -> redisProcessorUnderTest.set(key, value, time, timeType));
        assertTrue(businessException.getMessage().contains("设置的保存时间小于0"));
    }



}
