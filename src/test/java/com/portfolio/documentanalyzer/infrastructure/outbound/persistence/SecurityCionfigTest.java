package com.portfolio.documentanalyzer.infrastructure.outbound.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityCionfigTest {

    private final SecurityCionfig securityCionfig = new SecurityCionfig();

    @Test
    @DisplayName("CA-03: passwordEncoder retorna instancia funcional de BCryptPasswordEncoder")
    void passwordEncoderShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityCionfig.passwordEncoder();
        assertThat(encoder).isNotNull();
        String raw = "Password123!";
        String encoded = encoder.encode(raw);
        assertThat(encoder.matches(raw, encoded)).isTrue();
        assertThat(encoder.matches("WrongPassword", encoded)).isFalse();
    }
}
