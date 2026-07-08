package com.example.blog.security;

import com.example.blog.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.AllArgsConstructor;

@Component
public class JwtTokenProvider {

    private static final int MIN_SECRET_LENGTH = 32;
    private static final String PROD_PROFILE = "prod";

    private final SecretKey jwtSecret;
    private final long jwtExpiration;
    private final long refreshExpiration;
    private final Map<String, JwtClaims> claimsCache = new ConcurrentHashMap<>();

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration:604800000}") long refreshExpiration,
            @Value("${spring.profiles.active:default}") String activeProfiles) {
        validateSecret(secret, activeProfiles);
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    private void validateSecret(String secret, String activeProfiles) {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT secret must be at least " + MIN_SECRET_LENGTH + " characters long for security reasons");
        }

        boolean isProduction = activeProfiles != null &&
            (activeProfiles.contains(PROD_PROFILE) || activeProfiles.contains("production"));

        if (isProduction && secret.startsWith("mySecretKey")) {
            throw new IllegalStateException(
                "SECURITY ALERT: Using default JWT secret in production is not allowed! " +
                "Please set a secure JWT_SECRET environment variable.");
        }
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).getUserId();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getUsername();
    }

    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).getRole();
    }

    public boolean validateToken(String authToken) {
        try {
            getClaimsFromToken(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private JwtClaims getClaimsFromToken(String token) {
        // Check cache first
        JwtClaims cachedClaims = claimsCache.get(token);
        if (cachedClaims != null && !cachedClaims.isExpired()) {
            return cachedClaims;
        }

        // Parse and cache
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        JwtClaims jwtClaims = new JwtClaims(
            Long.parseLong(claims.getSubject()),
            claims.get("username", String.class),
            claims.get("role", String.class),
            claims.getExpiration()
        );

        // Cache the result (with expiration check)
        if (!jwtClaims.isExpired()) {
            claimsCache.put(token, jwtClaims);
        }

        return jwtClaims;
    }

    // Clean up expired cache entries periodically
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredCache() {
        claimsCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    @Data
    @AllArgsConstructor
    private static class JwtClaims {
        private Long userId;
        private String username;
        private String role;
        private Date expiration;

        public boolean isExpired() {
            return expiration != null && expiration.before(new Date());
        }
    }
}
