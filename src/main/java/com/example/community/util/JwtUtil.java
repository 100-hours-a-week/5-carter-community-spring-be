package com.example.community.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * 액세스 토큰을 생성합니다.
     *
     * @param email    사용자 이메일
     * @param userId   사용자 ID
     * @param nickname 사용자 닉네임
     * @return 생성된 액세스 토큰
     */
    public String generateToken(String email, Long userId, String nickname) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("nickname", nickname)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰을 생성합니다.
     *
     * @param email  사용자 이메일
     * @param userId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    public String generateRefreshToken(String email, Long userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰에서 모든 클레임을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 클레임
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    /**
     * JWT 토큰에서 이메일을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * JWT 토큰이 만료되었는지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 만료 여부
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * JWT 토큰이 유효한지 확인합니다.
     *
     * @param token JWT 토큰
     * @param email 이메일
     * @return 유효 여부
     */
    public boolean validateToken(String token, String email) {
        final String tokenEmail = extractAllClaims(token).getSubject();
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    /**
     * 리프레시 토큰이 유효한지 확인합니다.
     *
     * @param token 리프레시 토큰
     * @return 유효 여부
     */
    public boolean validateRefreshToken(String token) {
        return !isTokenExpired(token);
    }
}