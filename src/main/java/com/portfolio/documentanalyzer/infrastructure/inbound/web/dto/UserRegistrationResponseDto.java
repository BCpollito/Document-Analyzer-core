package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO de respuesta para un registro exitoso.
 * Garantiza que nunca se exponga el password_hash ni datos sensibles (CA-06).
 */
@Schema(description = "Respuesta de registro exitoso de usuario")
public record UserRegistrationResponseDto(

        @Schema(description = "Identificador único asignado al usuario", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nombre de usuario registrado", example = "john_doe")
        @JsonProperty("username")
        String username,

        @Schema(description = "Correo electrónico registrado", example = "john.doe@example.com")
        @JsonProperty("email")
        String email,

        @Schema(description = "Mensaje informativo del resultado", example = "Usuario registrado exitosamente")
        @JsonProperty("message")
        String message
) {
    public static UserRegistrationResponseDto of(UUID id, String username, String email) {
        return new UserRegistrationResponseDto(id, username, email, "Usuario registrado exitosamente");
    }
}
