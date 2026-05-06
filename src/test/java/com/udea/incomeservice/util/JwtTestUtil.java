package com.udea.incomeservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public final class JwtTestUtil {

    // Mismo valor que está en application-test.yaml
    private static final String SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1jaS1jZC1waXBlbGluZS0xMjM0NTY3ODkw";

    private JwtTestUtil() {}

    public static String generateToken(Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(Map.of("userId", userId))
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }
}