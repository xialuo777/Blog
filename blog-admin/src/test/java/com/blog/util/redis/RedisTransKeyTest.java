package com.blog.util.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RedisTransKeyTest {

    @Test
    public void testEmailKey() {
        String key = "test@example.com";
        String expectedKey = "user:emailCode:test@example.com";
        String actualKey = RedisTransKey.getEmailKey(key);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    public void testTokenKey() {
        String key = "user123";
        String expectedKey = "user:token:user123";
        String actualKey = RedisTransKey.getTokenKey(key);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    public void testRefreshTokenKey() {
        String key = "refresh123";
        String expectedKey = "user:refreshToken:refresh123";
        String actualKey = RedisTransKey.getRefreshTokenKey(key);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    public void testLoginKey() {
        String key = "login123";
        String expectedKey = "user:login:login123";
        String actualKey = RedisTransKey.getLoginKey(key);
        assertEquals(expectedKey, actualKey);
    }


    @Test
    public void testGetEmailKey() {
        String key = "test@example.com";
        String expectedKey = "user:emailCode:test@example.com";
        String actualKey = RedisTransKey.getEmailKey(key);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    public void testGetTokenKey() {
        String key = "user123";
        String expectedKey = "user:token:user123";
        String actualKey = RedisTransKey.getTokenKey(key);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    public void testGetRefreshTokenKey() {
        String key = "refresh123";
        String expectedKey = "user:refreshToken:refresh123";
        String actualKey = RedisTransKey.getRefreshTokenKey(key);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    public void testGetLoginKey() {
        String key = "login123";
        String expectedKey = "user:login:login123";
        String actualKey = RedisTransKey.getLoginKey(key);
        assertEquals(expectedKey, actualKey);
    }
}