package com.portfolio.documentanalyzer.domain.model;

import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserValueObjectsTest {

    @Test
    @DisplayName("UserId: Genera UUID válido y rechaza valores nulos")
    void userIdShouldWorkCorrectly() {
        UserId id = UserId.generate();
        assertThat(id.value()).isNotNull();

        UUID specificUuid = UUID.randomUUID();
        UserId specificId = UserId.of(specificUuid);
        assertThat(specificId.value()).isEqualTo(specificUuid);
        assertThat(specificId.toString()).isEqualTo(specificUuid.toString());

        assertThatThrownBy(() -> new UserId(null))
                .isInstanceOf(InvalidUserRegistrationException.class)
                .hasMessageContaining("El id del usuario es obligatorio");
    }

    @Test
    @DisplayName("Username: Normaliza con trim y valida longitud")
    void usernameShouldNormalizeAndValidate() {
        Username username = new Username("   john_doe   ");
        assertThat(username.value()).isEqualTo("john_doe");
        assertThat(username.toString()).isEqualTo("john_doe");

        assertThatThrownBy(() -> new Username(null))
                .isInstanceOf(InvalidUserRegistrationException.class);

        assertThatThrownBy(() -> new Username("   "))
                .isInstanceOf(InvalidUserRegistrationException.class);

        String tooLong = "a".repeat(51);
        assertThatThrownBy(() -> new Username(tooLong))
                .isInstanceOf(InvalidUserRegistrationException.class)
                .hasMessageContaining("longitud máxima");
    }

    @Test
    @DisplayName("Email: Normaliza a minúsculas, hace trim y valida formato")
    void emailShouldNormalizeAndValidate() {
        Email email = new Email("   JOHN.DOE@EXAMPLE.COM   ");
        assertThat(email.value()).isEqualTo("john.doe@example.com");

        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InvalidUserRegistrationException.class);

        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(InvalidUserRegistrationException.class);

        assertThatThrownBy(() -> new Email("correo-invalido"))
                .isInstanceOf(InvalidUserRegistrationException.class);

        assertThatThrownBy(() -> new Email("usuario..doblepunto@example.com"))
                .isInstanceOf(InvalidUserRegistrationException.class);
    }

    @Test
    @DisplayName("PasswordHash: Valida que no sea nulo ni vacío y oculta valor en toString")
    void passwordHashShouldValidateAndProtectToString() {
        PasswordHash hash = new PasswordHash("$2a$10$abcdef1234567890abcdef1234567890abcdef1234567890abcdef12");
        assertThat(hash.value()).isEqualTo("$2a$10$abcdef1234567890abcdef1234567890abcdef1234567890abcdef12");
        assertThat(hash.toString()).isEqualTo("[PROTECTED]");

        assertThatThrownBy(() -> new PasswordHash(null))
                .isInstanceOf(InvalidUserRegistrationException.class);

        assertThatThrownBy(() -> new PasswordHash("   "))
                .isInstanceOf(InvalidUserRegistrationException.class);
    }

    @Test
    @DisplayName("User: Entidad compone Value Objects y mantiene invariantes")
    void userShouldComposeValueObjects() {
        Username username = new Username("alice");
        Email email = new Email("alice@example.com");
        PasswordHash passwordHash = new PasswordHash("$2a$10$hashedAlicePassword123");

        User user = User.createRegistration(username, email, passwordHash);

        assertThat(user.getId().value()).isNotNull();
        assertThat(user.getUsername().value()).isEqualTo("alice");
        assertThat(user.getEmail().value()).isEqualTo("alice@example.com");
        assertThat(user.getPasswordHash().value()).isEqualTo("$2a$10$hashedAlicePassword123");
        assertThat(user.getCreatedAt()).isNotNull();

        // Mutación controlada mediante Value Objects
        user.changeUsername(new Username("alice_updated"));
        assertThat(user.getUsername().value()).isEqualTo("alice_updated");

        user.changeEmail(new Email("alice.new@example.com"));
        assertThat(user.getEmail().value()).isEqualTo("alice.new@example.com");

        user.changePasswordHash(new PasswordHash("$2a$10$newHashedPassword456"));
        assertThat(user.getPasswordHash().value()).isEqualTo("$2a$10$newHashedPassword456");
    }
}
