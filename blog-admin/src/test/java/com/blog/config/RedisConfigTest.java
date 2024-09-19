package com.blog.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RedisConfigTest {

    @Mock
    private RedisConnectionFactory factory;

    @InjectMocks
    private RedisConfig redisConfig;

    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    public void setUp() {
        redisTemplate = redisConfig.redisTemplate(factory);
    }

    @Test
    public void redisTemplate_ShouldSetConnectionFactory() {
        assertEquals(factory, redisTemplate.getConnectionFactory());
    }

    @Test
    public void redisTemplate_ShouldUseStringRedisSerializerForKeys() {
        assertEquals(StringRedisSerializer.class, redisTemplate.getKeySerializer().getClass());
    }

    @Test
    public void redisTemplate_ShouldUseGenericJackson2JsonRedisSerializerForValues() {
        assertEquals(GenericJackson2JsonRedisSerializer.class, redisTemplate.getValueSerializer().getClass());
    }
}
