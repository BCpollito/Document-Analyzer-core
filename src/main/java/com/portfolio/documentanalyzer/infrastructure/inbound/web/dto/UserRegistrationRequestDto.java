package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la solicitud de registro de usuarios.
 * Primera línea de defensa para validaciones de formato y tamaño.
 */
@Schema(description = "Datos para el registro de un nuevo usuario")
public record UserRegistrationRequestDto(

        @Schema(description = "Nombre de usuario único", example = "john_doe")
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        String username,

        @Schema(description = "Correo electrónico único", example = "john.doe@example.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 255, message = "El email no debe exceder los 255 caracteres")
        String email,

        @Schema(description = "Contraseña en texto plano a validar y codificar", example = "Secret123#")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
        )
        String password
) {

    @Override
    public String toString() {
        return "UserRegistrationRequestDto[username=" + username + ", email=" + email + ", password=***]";
    }
}
