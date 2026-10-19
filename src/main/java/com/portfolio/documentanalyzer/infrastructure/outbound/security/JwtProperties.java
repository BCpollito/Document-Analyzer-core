package com.portfolio.documentanalyzer.infrastructure.outbound.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
@EnableConfigurationProperties(JwtProperties.class)
public record JwtProperties(
        String secret,
        Duration expiration
) {
}
