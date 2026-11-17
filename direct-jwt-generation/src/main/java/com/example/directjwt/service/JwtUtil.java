package com.example.directjwt.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.directjwt.dto.JwtRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

/**
 * Reusable JWT helper: generates and verifies HS256 signed tokens.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtUtil(SecretKey secretKey,
                   @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.secretKey = secretKey;
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Builds a signed JWT from the JSON payload (username + role).
     *
     * @return compact token string: HEADER.PAYLOAD.SIGNATURE
     */
    public String generateToken(JwtRequest request) {
        String username = request.username();
        String role = request.role();

        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMinutes * 60_000L);

        return Jwts.builder()
                .subject(username)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifies the signature and expiration, then returns the claims.
     *
     * @throws io.jsonwebtoken.ExpiredJwtException    if the token is expired
     * @throws io.jsonwebtoken.security.SignatureException if the signature does not match
     * @throws JwtException                           for any other invalid token
     */
    public Claims verifyToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
