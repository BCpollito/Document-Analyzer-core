package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageRoleDto(
        @JsonProperty("role")
        String role,

        @JsonProperty("content")
        String content
) {
    public static MessageRoleDto of(String role, String content) {
        return new MessageRoleDto(role, content);
    }

    public static MessageRoleDto user(String content) {
        return new MessageRoleDto("USER", content);
    }

    public static MessageRoleDto assistant(String content) {
        return new MessageRoleDto("ASSISTANT", content);
    }

    public static MessageRoleDto system(String content) {
        return new MessageRoleDto("SYSTEM", content);
    }
}
