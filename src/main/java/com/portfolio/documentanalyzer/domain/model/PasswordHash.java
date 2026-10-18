package com.portfolio.documentanalyzer.domain.model;

import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;

/**
 * Objeto de Valor (Value Object) inmutable que encapsula el hash de la contraseña.
 * Garantiza que solo se almacenen hashes no vacíos que cumplan las restricciones de longitud.
 */
public record PasswordHash(String value) {

    public static final int MAX_LENGTH = 255;

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new InvalidUserRegistrationException("El hash de la contraseña es obligatorio");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserRegistrationException("El hash de la contraseña excede la longitud máxima permitida");
        }
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
