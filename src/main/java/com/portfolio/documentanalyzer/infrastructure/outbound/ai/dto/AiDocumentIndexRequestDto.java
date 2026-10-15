package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiDocumentIndexRequestDto(
        @JsonProperty("document_id")
        String documentId,

        @JsonProperty("content")
        String content,

        @JsonProperty("metadata")
        Map<String, String> metadata
) {
    public AiDocumentIndexRequestDto(String documentId, String content) {
        this(documentId, content, null);
    }
}
