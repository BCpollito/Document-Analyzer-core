package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @Email
        @NotBlank(message = "El email no puede ser nulo")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacia")
        String password
) {
}
