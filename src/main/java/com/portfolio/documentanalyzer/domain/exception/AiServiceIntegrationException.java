package com.portfolio.documentanalyzer.domain.exception;

import lombok.Getter;

@Getter
public class AiServiceIntegrationException extends RuntimeException {

    public static final String ERROR_AI_TIMEOUT = "AI_TIMEOUT";
    public static final String ERROR_AI_PROCESSING = "AI_PROCESSING_ERROR";

    private final String errorCode;

    public AiServiceIntegrationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiServiceIntegrationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
