package com.blog.util;


import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Component
@Slf4j
public class JwtProcessor {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Token令牌验证
     * @param token
     * @param userId
     * @return Boolean
     */
    public Boolean validateToken(String token, Long userId) {
        final Long userIdFromToken = extractUserId(token);
        return (userIdFromToken.equals(userId) && !isTokenExpired(token));
    }
    public Long extractUserId(String token) {
        if (token ==null){
            log.error("token为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"token为空");
        }
        if (isTokenExpired(token)){
            log.error("token已失效");
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED,"token失效");
        }
        Long userId = Long.valueOf(extractClaim(token, Claims::getSubject));
        return userId;
    }

    /**
     * 生成Token令牌
     * @param userId
     * @return String
     */
    public String generateToken(Long userId) {
        return createToken(new HashMap<>(), userId, jwtExpiration);
    }

    /**
     * 刷新令牌
     * @param userId
     * @return String
     */

    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userId, jwtExpiration*4*24*7);
    }


    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        boolean ret;
        try {
            ret = extractExpiration(token).before(new Date());
        }catch (Exception e){
            log.error("token解析失败,invalid token");
            throw new BusinessException(ErrorCode.TOKEN_ERROR,"token解析失败,invalid token");
        }
        return ret;
    }


    private String createToken(Map<String, Object> claims, Long userId, long expiration) {
        return Jwts.builder().setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()+ 8 * 3600 * 1000))
                .setExpiration(new Date(System.currentTimeMillis()+ 8 * 3600 * 1000 + expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

}