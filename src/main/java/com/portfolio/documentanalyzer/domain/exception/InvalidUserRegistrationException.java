package com.portfolio.documentanalyzer.domain.exception;

/**
 * Se lanza cuando los datos de registro de un usuario incumplen las invariantes del dominio
 * (por ejemplo, email con formato inválido o passwordHash vacío).
 */
public class InvalidUserRegistrationException extends RuntimeException {

    public InvalidUserRegistrationException(String message) {
        super(message);
    }

    public InvalidUserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
