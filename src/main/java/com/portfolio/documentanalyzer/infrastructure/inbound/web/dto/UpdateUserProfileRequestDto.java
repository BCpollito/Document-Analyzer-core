package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

public record UpdateUserProfileRequestDto(
        String username,
        String email
) {
}