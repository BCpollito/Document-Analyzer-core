package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiRagQueryResponseDto(
        @JsonProperty("status")
        String status,

        @JsonProperty("data")
        Data data,

        @JsonProperty("error")
        Error error
) {
    public AiRagQueryResponseDto(String status, Data data, AiErrorDto aiError) {
        this(status, data, aiError != null ? new Error(aiError.code(), aiError.message()) : null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Data(
            @JsonProperty("answer")
            String answer,

            @JsonProperty("sources")
            List<String> sources
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(
            @JsonProperty("code")
            String code,

            @JsonProperty("message")
            String message
    ) {
        public AiErrorDto toAiErrorDto() {
            return new AiErrorDto(code, message);
        }
    }
}
