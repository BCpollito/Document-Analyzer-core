package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import java.time.Instant;

public record LoginResponseDto(
        String token,
        String tokenType,
        Instant expiresAt
) {
}
