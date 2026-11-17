package com.example.directjwt.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a successful token verification")
public record VerifyResponse(
        boolean valid,
        String username,
        String role,
        Instant issuedAt,
        Instant expiresAt) {
}
