package com.blog.service;

import com.blog.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Token令牌验证
     * @param token
     * @param user
     * @return Boolean
     */
    public Boolean validateToken(String token, User user) {
        final String emailFromToken = extractEmail(token);
        return (emailFromToken.equals(user.getEmail()) && !isTokenExpired(token));
    }

    /**
     * 生成Token令牌
     * @param user
     * @return String
     */
    public String generateToken(User user) {
        return createToken(new HashMap<>(), user, jwtExpiration*2);
    }

    /**
     * 刷新令牌
     * @param user
     * @return String
     */

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user, jwtExpiration);
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
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


    private String createToken(Map<String, Object> claims, User user, long expiration) {
        return Jwts.builder().setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }

}