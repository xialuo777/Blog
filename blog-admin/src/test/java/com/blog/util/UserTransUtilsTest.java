package com.blog.util;


import com.blog.entity.User;
import com.blog.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserTransUtilsTest {

    @Test
    void testGetUserMap_ValidUser_ReturnsCorrectMap() {
        final User user = new User();
        user.setUserId(1L);
        user.setAccount("testAccount");
        user.setNickName("testNickName");
        final Map<String, Object> result = UserTransUtils.getUserMap(user);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(user.getUserId(), result.get("userId"));
        assertEquals(user.getAccount(), result.get("account"));
        assertEquals(user.getNickName(), result.get("nickName"));
    }

    @Test
    void testGetUserMap_EmptyFields_ReturnsMapWithEmptyValues() {
        final User user = new User();
        user.setUserId(1L);

        final Map<String, Object> result = UserTransUtils.getUserMap(user);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(user.getUserId(), result.get("userId"));
        assertEquals(null, result.get("account"));
        assertEquals(null, result.get("nickName"));
    }

    @Test
    void testGetUserMap_NullUser_ThrowsNullPointerException() {
        User user = null;
        assertThrows(BusinessException.class, ()->UserTransUtils.getUserMap(user));
    }
}
