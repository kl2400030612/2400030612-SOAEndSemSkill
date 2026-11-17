package com.example.directjwt.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Creates the HS256 signing key (from application.properties / JWT_SECRET)
 * and the Swagger/OpenAPI description.
 */
@Configuration
public class JwtConfig {

    /** HS256 requires a key of at least 256 bits = 32 bytes. */
    private static final int MIN_SECRET_BYTES = 32;

    @Bean
    public SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret) {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret is too short. HS256 needs at least 32 characters (256 bits). "
                            + "Set the JWT_SECRET environment variable to a longer value.");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    @Bean
    public OpenAPI directJwtOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Direct JWT Generation API")
                .version("1.0.0")
                .description("Generate and verify HS256 signed JWT tokens from a JSON payload."));
    }
}
