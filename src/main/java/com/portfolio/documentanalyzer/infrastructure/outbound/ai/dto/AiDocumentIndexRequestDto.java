package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiDocumentIndexRequestDto(
        @JsonProperty("document_id")
        String documentId,

        @JsonProperty("user_id")
        String userId,

        @JsonProperty("content")
        String content,

        @JsonProperty("metadata")
        Map<String, String> metadata
) {
    public AiDocumentIndexRequestDto(String documentId, String content) {
        this(documentId, null, content, null);
    }

    public AiDocumentIndexRequestDto(String documentId, String content, Map<String, String> metadata) {
        this(documentId, null, content, metadata);
    }

    public AiDocumentIndexRequestDto(String documentId, String userId, String content) {
        this(documentId, userId, content, null);
    }
}
