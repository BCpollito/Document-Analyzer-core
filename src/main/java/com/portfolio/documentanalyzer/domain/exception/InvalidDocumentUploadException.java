package com.portfolio.documentanalyzer.domain.exception;

public class InvalidDocumentUploadException extends RuntimeException {
    public InvalidDocumentUploadException(String message) {
        super(message);
    }
}
