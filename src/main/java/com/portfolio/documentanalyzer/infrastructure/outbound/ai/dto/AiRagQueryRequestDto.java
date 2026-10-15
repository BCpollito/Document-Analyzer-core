package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiRagQueryRequestDto(
        @JsonProperty("query")
        String query,

        @JsonProperty("conversation_id")
        String conversationId,

        @JsonProperty("history")
        List<MessageRoleDto> history
) {
    public AiRagQueryRequestDto(String query, String conversationId) {
        this(query, conversationId, List.of());
    }
}
