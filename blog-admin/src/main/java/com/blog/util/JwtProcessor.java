package com.blog.util;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtProcessor {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private int jwtExpiration;

    /**
     * Token令牌验证
     *
     * @param token
     * @param userId
     * @return Boolean
     */
    public Boolean validateToken(String token, Long userId) {
        final Map<String, Object> userMap = extractUserMap(token);
        return userMap.get("id").equals(userId);
    }


    /**
     * 生成Token令牌
     *
     * @param userMap
     * @return String
     */
    public String generateToken(Map<String, Object> userMap) {
        return createToken(userMap, jwtExpiration);
    }

    public Map<String, Object> extractUserMap(String token) {
        Map<String, Object> map = extractAllClaims(token);
        Map<String, Object> userMap = new HashMap<>(3);
        userMap.put("userId", map.get("userId"));
        userMap.put("nickName", map.get("nickName"));
        userMap.put("account", map.get("account"));
        return userMap;
    }

    /**
     * 刷新令牌
     *
     * @param userMap
     * @return String
     */

    public String generateRefreshToken(Map<String, Object> userMap) {
        return createToken(userMap, jwtExpiration * 4 * 24 * 7);
    }

    private Claims extractAllClaims(String token) {
        Claims claims;
        try {
            claims =  Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException | SignatureException e){
            log.error("非法的令牌格式");
            throw new BusinessException(ErrorCode.TOKEN_ERROR,"非法的令牌格式");
        }catch (ExpiredJwtException e){
            log.error("令牌已过期");
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED,"令牌已过期");
        }
        return claims;
    }

    private String createToken(Map<String, Object> claims, int expiration) {
        final Date date = DateUtils.addMinutes(new Date(),expiration);
        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

}
