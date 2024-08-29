package com.blog.util;

import com.blog.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JwtUtils {
    @Value("${security.jwt.secret-key}")
    private static String secretKey;
    @Value("${security.jwt.expiration-time}")
    private static long jwtExpirationTime;

    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private static Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJwt(token)
                .getBody();
    }

    private static Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public static String extractEmailCode(String token) {
        return extractClaim(token, Claims::getAudience);
    }
    public static String generateToken(Map<String, Object> extraClaims, User user) {
        return buildToken(extraClaims, user, jwtExpirationTime);
    }
    public static String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    private static String buildToken(
            Map<String, Object> extraClaims,
            User user,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static long getExpirationTime() {
        return jwtExpirationTime;
    }
    public static boolean isTokenValid(String token, User user) {
        final String userEmail = extractUserEmail(token);
        return (userEmail.equals(user.getEmail())) && !isTokenExpired(token);
    }
    private static boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    private static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}