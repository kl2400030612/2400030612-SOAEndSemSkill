package com.example.directjwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Token to verify")
public record VerifyRequest(

        @Schema(description = "The JWT string to verify")
        @NotBlank(message = "token is required")
        String token) {
}
