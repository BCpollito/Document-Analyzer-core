package com.portfolio.documentanalyzer.domain.vo;

import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;

import java.util.UUID;

/**
 * Objeto de Valor (Value Object) inmutable que encapsula el identificador único del usuario.
 */
public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new InvalidUserRegistrationException("El id del usuario es obligatorio");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
