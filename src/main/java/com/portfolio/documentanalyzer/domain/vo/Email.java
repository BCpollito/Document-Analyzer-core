package com.portfolio.documentanalyzer.domain.model;

import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Objeto de Valor (Value Object) inmutable que encapsula el correo electrónico del usuario.
 * Garantiza normalización a minúsculas, eliminación de espacios y validación de formato RFC.
 */
public record Email(String value) {

    public static final int MAX_LENGTH = 255;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9](?:[A-Za-z0-9._%+-]*[A-Za-z0-9])?@"
                    + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidUserRegistrationException("El email es obligatorio");
        }
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH || value.contains("..") || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidUserRegistrationException("El formato del email no es válido");
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
