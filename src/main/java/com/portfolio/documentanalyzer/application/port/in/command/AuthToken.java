package com.portfolio.documentanalyzer.application.port.in.command;

import java.time.Instant;

public record AuthToken(
        String accesTokenm,
        Instant expiresAt
) {
}
