package com.example.directjwt.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generated JWT and its timestamps")
public record JwtResponse(
        @Schema(description = "Signed JWT in the form HEADER.PAYLOAD.SIGNATURE") String token,
        @Schema(example = "Bearer") String tokenType,
        @Schema(example = "Afreen") String username,
        @Schema(example = "ADMIN") String role,
        @Schema(description = "Issued-at time (UTC)") Instant issuedAt,
        @Schema(description = "Expiration time (UTC)") Instant expiresAt) {
}
