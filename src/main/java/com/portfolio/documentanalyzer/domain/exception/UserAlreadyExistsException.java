package com.portfolio.documentanalyzer.domain.exception;

/** Se lanza cuando se intenta registrar un email o un usuario que ya existe (conflicto, HTTP 409). */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
