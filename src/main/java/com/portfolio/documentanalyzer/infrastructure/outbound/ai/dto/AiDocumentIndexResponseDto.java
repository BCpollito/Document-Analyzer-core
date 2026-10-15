package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiDocumentIndexResponseDto(
        @JsonProperty("status")
        String status,

        @JsonProperty("data")
        Data data,

        @JsonProperty("error")
        Error error
) {
    public AiDocumentIndexResponseDto(String status, Data data, AiErrorDto aiError) {
        this(status, data, aiError != null ? new Error(aiError.code(), aiError.message()) : null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Data(
            @JsonProperty("document_id")
            String documentId,

            @JsonProperty("chunks_processed")
            Integer chunksProcessed
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
