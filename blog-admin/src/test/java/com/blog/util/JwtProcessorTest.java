package com.blog.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.blog.constant.Constant;
import com.blog.exception.BusinessException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class JwtProcessorTest {
    private JwtProcessor jwtProcessorUnderTest;
    private final String secretKey = "3b1eef9f4d6944ca7c864c748fc4fc1c7ee66db08fc6288d85a85445832378526ab5c00e63bdabfbf0243acd1934bdfb661cc06837378d8df2c674e5365cede1";
    private final int jwtExpiration = 15;


    @BeforeEach
    void setUp() {
        jwtProcessorUnderTest = new JwtProcessor();
        ReflectionTestUtils.setField(jwtProcessorUnderTest, "secretKey", "3b1eef9f4d6944ca7c864c748fc4fc1c7ee66db08fc6288d85a85445832378526ab5c00e63bdabfbf0243acd1934bdfb661cc06837378d8df2c674e5365cede1");
        ReflectionTestUtils.setField(jwtProcessorUnderTest, "jwtExpiration", 15);
    }

    @Test
    void testValidateToken_ValidTokenAndUserId_ReturnsTrue() {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1L);
        userMap.put("nick_name", "TestUser");
        userMap.put("account", "testAccount");
        String token = jwtProcessorUnderTest.generateToken(userMap);

        boolean isValid = jwtProcessorUnderTest.validateToken(token, 1L);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_ValidTokenAndDifferentUserId_ReturnsFalse() {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1L);
        userMap.put("nick_name", "TestUser");
        userMap.put("account", "testAccount");
        String token = jwtProcessorUnderTest.generateToken(userMap);
        Long userId = 2L;

        boolean result = jwtProcessorUnderTest.validateToken(token, userId);
        assertFalse(result);
    }

    @Test
    void testValidateToken_InvalidToken_ThrowsBusinessException() {
        String token = "invalidToken";
        Long userId = 1L;

        BusinessException businessException = assertThrows(BusinessException.class, () -> jwtProcessorUnderTest.validateToken(token, userId));
        assertTrue(businessException.getMessage().contains("非法的令牌格式"));
    }

    @Test
    void testValidateToken_ExpiredToken_ThrowsBusinessException() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constant.ID, 1L);
        claims.put(Constant.NICK_NAME, "TestUser");
        claims.put(Constant.ACCOUNT, "testAccount");
        Long userId = 1L;

        Date expirationDate = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24)); // 一天前
        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, "3b1eef9f4d6944ca7c864c748fc4fc1c7ee66db08fc6288d85a85445832378526ab5c00e63bdabfbf0243acd1934bdfb661cc06837378d8df2c674e5365cede1")
                .compact();
        BusinessException businessException = assertThrows(BusinessException.class, () -> jwtProcessorUnderTest.validateToken(expiredToken, userId));
        assertTrue(businessException.getMessage().contains("令牌已过期"));
    }
    
    @Test
    void testGenerateToken() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(Constant.ID, 1L);
        userMap.put(Constant.NICK_NAME, "TestUser");
        userMap.put(Constant.ACCOUNT, "testAccount");

        String token = jwtProcessorUnderTest.generateToken(userMap);

        assertThat(token).isNotBlank();
    }
    @Test
    void testGenerateRefreshToken() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1L);
        userMap.put("nick_name", "TestUser");
        userMap.put("account", "testAccount");

        String refreshToken = jwtProcessorUnderTest.generateRefreshToken(userMap);

        assertThat(refreshToken).isNotBlank();
    }
    @Test
    void testExtractUserMap_ValidToken_ReturnsUserMap() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constant.ID, 1L);
        claims.put(Constant.NICK_NAME, "TestUser");
        claims.put(Constant.ACCOUNT, "testAccount");

        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        Map<String, Object> userMap = jwtProcessorUnderTest.extractUserMap(token);

        assertNotNull(userMap);
        assertEquals(3, userMap.size());
        assertEquals(1, userMap.get(Constant.ID));
        assertEquals("TestUser", userMap.get(Constant.NICK_NAME));
        assertEquals("testAccount", userMap.get(Constant.ACCOUNT));
    }

    @Test
    void testExtractUserMap_InvalidToken_ThrowsBusinessException() {
        String invalidToken = "invalidToken";

        BusinessException businessException = assertThrows(BusinessException.class, () -> jwtProcessorUnderTest.extractUserMap(invalidToken));
        assertTrue(businessException.getMessage().contains("非法的令牌格式"));
    }

    @Test
    void testExtractUserMap_ExpiredToken_ThrowsBusinessException() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constant.ID, 1L);
        claims.put(Constant.NICK_NAME, "TestUser");
        claims.put(Constant.ACCOUNT, "testAccount");

        Date expirationDate = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24)); // 一天前
        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        BusinessException businessException = assertThrows(BusinessException.class, () -> jwtProcessorUnderTest.extractUserMap(expiredToken));
        assertTrue(businessException.getMessage().contains("令牌已过期"));
    }



}
