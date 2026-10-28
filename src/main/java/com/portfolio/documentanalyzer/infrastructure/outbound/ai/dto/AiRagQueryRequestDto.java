package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiRagQueryRequestDto(
        @JsonProperty("query")
        String query,

        @JsonProperty("user_id")
        String userId,

        @JsonProperty("conversation_id")
        String conversationId,

        @JsonProperty("history")
        List<MessageRoleDto> history
) {
    public AiRagQueryRequestDto(String query, String conversationId) {
        this(query, null, conversationId, List.of());
    }

    public AiRagQueryRequestDto(String query, String conversationId, List<MessageRoleDto> history) {
        this(query, null, conversationId, history);
    }

    public AiRagQueryRequestDto(String query, String userId, String conversationId) {
        this(query, userId, conversationId, List.of());
    }

    public AiRagQueryRequestDto(String query, String userId, String conversationId, List<MessageRoleDto> history) {
        this.query = query;
        this.userId = userId;
        this.conversationId = conversationId;
        this.history = history != null ? history : List.of();
    }
}
