package com.example.directjwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * JSON payload sent by the client to request a token.
 */
@Schema(description = "User information used to build the JWT")
public record JwtRequest(

        @Schema(example = "Afreen", description = "Username to store in the token")
        @NotBlank(message = "username is required")
        String username,

        @Schema(example = "ADMIN", description = "Role to store in the token")
        @NotBlank(message = "role is required")
        String role) {
}
