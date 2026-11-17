package com.example.directjwt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import com.example.directjwt.dto.JwtRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

class JwtUtilTest {

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private final SecretKey secretKey = key("unit-test-secret-key-0123456789-abcdefghij");
    private final JwtUtil jwtUtil = new JwtUtil(secretKey, 60);
    private final JwtRequest request = new JwtRequest("Afreen", "ADMIN");

    @Test
    void tokenHasThreeParts() {
        String token = jwtUtil.generateToken(request);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void validTokenContainsUsernameAndRole() {
        Claims claims = jwtUtil.verifyToken(jwtUtil.generateToken(request));
        assertEquals("Afreen", claims.get("username", String.class));
        assertEquals("ADMIN", claims.get("role", String.class));
        assertEquals("Afreen", claims.getSubject());
    }

    @Test
    void tamperedSignatureIsRejected() {
        String token = jwtUtil.generateToken(request);
        int lastDot = token.lastIndexOf('.');
        char first = token.charAt(lastDot + 1);
        char replacement = first == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, lastDot + 1) + replacement + token.substring(lastDot + 2);
        assertThrows(JwtException.class, () -> jwtUtil.verifyToken(tampered));
    }

    @Test
    void tokenSignedWithDifferentKeyIsRejected() {
        JwtUtil other = new JwtUtil(key("another-different-secret-0123456789-abcdefgh"), 60);
        String foreignToken = other.generateToken(request);
        assertThrows(JwtException.class, () -> jwtUtil.verifyToken(foreignToken));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtUtil expiring = new JwtUtil(secretKey, -1); // already expired
        String token = expiring.generateToken(request);
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.verifyToken(token));
    }

    @Test
    void garbageIsRejected() {
        assertThrows(JwtException.class, () -> jwtUtil.verifyToken("not-a-jwt"));
    }
}
