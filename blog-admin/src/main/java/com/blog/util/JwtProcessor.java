package com.blog.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Component
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
        return createToken(claims, userId, jwtExpiration*2);
    }

    public Long extractUserId(String token) {
        return Long.valueOf(extractClaim(token, Claims::getSubject));
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
        return extractExpiration(token).before(new Date());
    }


    private String createToken(Map<String, Object> claims, Long userId, long expiration) {
        return Jwts.builder().setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }

}