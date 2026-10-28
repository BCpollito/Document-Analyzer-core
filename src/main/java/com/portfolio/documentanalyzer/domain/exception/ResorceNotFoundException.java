package com.portfolio.documentanalyzer.domain.exception;

public class ResorceNotFoundException extends RuntimeException {
    public ResorceNotFoundException(String message) {
        super(message);
    }
}
