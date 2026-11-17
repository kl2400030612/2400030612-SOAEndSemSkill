package com.example.directjwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.directjwt.dto.ErrorResponse;
import com.example.directjwt.dto.JwtRequest;
import com.example.directjwt.dto.JwtResponse;
import com.example.directjwt.dto.VerifyRequest;
import com.example.directjwt.dto.VerifyResponse;
import com.example.directjwt.service.JwtUtil;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/jwt")
@Tag(name = "JWT", description = "Generate and verify JWT tokens")
public class JwtController {

    private final JwtUtil jwtUtil;

    public JwtController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Generate a JWT from a JSON payload",
            description = "Takes username and role, returns an HS256 signed token.")
    @ApiResponse(responseCode = "200", description = "Token generated")
    @ApiResponse(responseCode = "400", description = "Missing or invalid fields",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/generate")
    public ResponseEntity<JwtResponse> generate(@Valid @RequestBody JwtRequest request) {
        String token = jwtUtil.generateToken(request);

        // Read the timestamps back from the token we just created
        Claims claims = jwtUtil.verifyToken(token);

        JwtResponse response = new JwtResponse(
                token,
                "Bearer",
                request.username(),
                request.role(),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify a JWT",
            description = "Checks the signature and the expiration time.")
    @ApiResponse(responseCode = "200", description = "Token is valid")
    @ApiResponse(responseCode = "400", description = "Token field missing",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Token is invalid, tampered with, or expired",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@Valid @RequestBody VerifyRequest request) {
        Claims claims = jwtUtil.verifyToken(request.token().trim());

        VerifyResponse response = new VerifyResponse(
                true,
                claims.get("username", String.class),
                claims.get("role", String.class),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
        return ResponseEntity.ok(response);
    }
}
