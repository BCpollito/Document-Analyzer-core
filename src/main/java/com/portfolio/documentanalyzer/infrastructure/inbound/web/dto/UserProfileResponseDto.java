package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

public record UserProfileResponseDto(
        String id,
        String username,
        String email
) {
}
