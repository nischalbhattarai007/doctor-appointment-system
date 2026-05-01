package com.doctorappointment.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Singleton
public class JwtUtil {
    private final String secret;
    private final long expiryMs;
    private final long refreshExpiryMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-ms:3600000}") long expiryMs,
            @Value("${jwt.refresh-expiry-ms}")  long refreshExpiryMs ) {
        this.secret = secret;
        this.expiryMs = expiryMs;
        this.refreshExpiryMs=refreshExpiryMs;
    }
    public String generateToken(String email,String role){
        return buildToken(email,role,expiryMs,"access");
    }
    public String refreshToken(String email, String role){
        return buildToken(email,role,refreshExpiryMs,"refresh");
    }
        private String buildToken(String email, String role,long expiryMs,String tokenType) {
        Date now = new Date();
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(email)
                .claim("role", role)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getKey())
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("type", String.class));
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
