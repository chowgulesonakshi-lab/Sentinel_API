package org.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final SecretKey secretKey;

    public JwtUtil(@Value("${app.jwt.secret:}") String encodedSecret) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured as Base64 containing at least 32 random bytes");
        }
        try {
            byte[] key = Base64.getDecoder().decode(encodedSecret);
            if (key.length < 32) throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes");
            this.secretKey = new SecretKeySpec(key, "HmacSHA256");
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET must be valid Base64", ex);
        }
    }

    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder().claims(Map.of()).subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + 1000L * 60 * 60 * 10))
                .signWith(secretKey).compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = extractAllClaims(token);
            return username.equals(claims.getSubject())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
