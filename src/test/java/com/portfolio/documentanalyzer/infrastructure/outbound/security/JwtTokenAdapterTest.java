package com.portfolio.documentanalyzer.infrastructure.outbound.security;

import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.domain.vo.Email;
import com.portfolio.documentanalyzer.domain.vo.PasswordHash;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import com.portfolio.documentanalyzer.domain.vo.Username;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private static final String TEST_SECRET = "superSecretKeyForJwtTokenAdapterTestingMustBeAtLeast256BitsLong!";
    private static final Duration EXPIRATION = Duration.ofMinutes(60);

    private JwtTokenAdapter jwtTokenAdapter;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(TEST_SECRET, EXPIRATION);
        this.jwtTokenAdapter = new JwtTokenAdapter(properties);
    }

    @Test
    @DisplayName("Test 1: Emite un token JWT válido con subject (userId) y fecha de expiración")
    void issueShouldGenerateValidJwtToken() {
        UUID userId = UUID.randomUUID();
        User user = User.reconstitute(
                new UserId(userId),
                new Username("john_doe"),
                new Email("john.doe@example.com"),
                new PasswordHash("$2a$10$dummyHashValuetest1234567890"),
                Instant.now()
        );

        AuthToken authToken = jwtTokenAdapter.issue(user);

        assertThat(authToken).isNotNull();
        assertThat(authToken.accesTokenm()).isNotBlank();
        assertThat(authToken.expiresAt()).isAfter(Instant.now());

        // Parsear el token para verificar su contenido
        Claims claims = jwtTokenAdapter.parse(authToken.accesTokenm());
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email")).isNotNull();
    }

    @Test
    @DisplayName("Test 2: Falla al parsear un token con firma adulterada o inválida")
    void parseShouldThrowOnTamperedToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.tamperedSignature";

        assertThatThrownBy(() -> jwtTokenAdapter.parse(invalidToken))
                .isInstanceOf(JwtException.class);
    }
}
