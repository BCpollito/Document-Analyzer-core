package com.portfolio.documentanalyzer.domain.vo;

import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;

/**
 * Objeto de Valor (Value Object) inmutable que encapsula el nombre de usuario y sus invariantes.
 */
public record Username(String value) {

    public static final int MAX_LENGTH = 50;

    public Username {
        if (value == null || value.isBlank()) {
            throw new InvalidUserRegistrationException("El username es obligatorio");
        }
        value = value.strip();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserRegistrationException(
                    "El username excede la longitud máxima de " + MAX_LENGTH + " caracteres");
        }
    }

    public static Username of(String value) {
        return new Username(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
