package com.urlshortener.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public JwtUtil(
      @Value("${app.jwt.access-token-secret}") String accessSecret,
            @Value("${app.jwt.refresh-token-secret}") String refreshSecret)
    {
        this.accessTokenKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
         this.refreshTokenKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(String email){
        return Jwts.builder()
        .subject(email)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis()+accessTokenExpirationMs))
        .id(UUID.randomUUID().toString())
        .signWith(accessTokenKey)
        .compact();
    }

    public String generateRefreshToken(String email){
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+refreshTokenExpirationMs))
                .id(UUID.randomUUID().toString())
                .signWith(refreshTokenKey)
                .compact();
    }

    public String extractEmailFromToken(String token){
        return parseClaims(token,accessTokenKey).getSubject();
    }

    public String extractEmailFromRefreshToken(String token) {
        return parseClaims(token, refreshTokenKey).getSubject();
    }

    private Claims parseClaims(String token , SecretKey key){
        return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    }

    public boolean validateAccessToken(String token){
        try {
            parseClaims(token, accessTokenKey);
            return true;
        }  catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

     public boolean validateRefreshToken(String token) {
        try {
            parseClaims(token, refreshTokenKey);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
