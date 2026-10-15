package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        @JsonProperty("status")
        String status,

        @JsonProperty("error")
        ErrorDetail error
) {
    public static ErrorResponseDto of(String code, String message) {
        return new ErrorResponseDto("error", new ErrorDetail(code, message));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
            @JsonProperty("code")
            String code,

            @JsonProperty("message")
            String message
    ) {}
}
