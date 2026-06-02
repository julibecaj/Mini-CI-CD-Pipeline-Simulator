package com.pipes.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Stateless JWT utility — Singleton bean (GoF Singleton via Spring — R9 secondary).
 * Generates and validates HS256 tokens.
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${pipes.jwt.secret}")
    private String jwtSecret;

    @Value("${pipes.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey key() {
        // Derive a SecretKey from the configured secret (must be >= 256 bits for HS256)
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters (256 bits).");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Generate a signed JWT for the given username. */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /** Extract the username claim from a valid token. */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Validate the token; return false (not throw) on any failure. */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
