package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.application.port.in.command.LoginCommand;
import com.portfolio.documentanalyzer.application.port.out.PasswordHasherPort;
import com.portfolio.documentanalyzer.application.port.out.TokenIssuePort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.InvalidCredentialException;
import com.portfolio.documentanalyzer.domain.model.Email;
import com.portfolio.documentanalyzer.domain.model.PasswordHash;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.model.UserId;
import com.portfolio.documentanalyzer.domain.model.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordHasherPort hasher;

    @Mock
    private TokenIssuePort tokenIssuePort;

    @InjectMocks
    private LoginService loginService;

    private User createSampleUser(String emailStr, String rawHash) {
        return User.reconstitute(
                new UserId(UUID.randomUUID()),
                new Username("testuser"),
                new Email(emailStr),
                new PasswordHash(rawHash),
                Instant.now()
        );
    }

    @Test
    @DisplayName("Test 1: Login exitoso cuando el usuario existe y la contraseña es correcta")
    void loginShouldSucceedWhenCredentialsAreValid() {
        // Arrange
        String email = "john.doe@example.com";
        String rawPassword = "ValidPassword123!";
        String storedHash = "$2a$10$exampleHashedPasswordValueForTest123456789";
        User user = createSampleUser(email, storedHash);

        LoginCommand command = new LoginCommand(email, rawPassword);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        AuthToken expectedToken = new AuthToken("sample.jwt.token", expiresAt);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches(rawPassword, storedHash)).thenReturn(true);
        when(tokenIssuePort.issue(user)).thenReturn(expectedToken);

        // Act
        AuthToken resultToken = loginService.login(command);

        // Assert
        assertThat(resultToken).isNotNull();
        assertThat(resultToken.accesTokenm()).isEqualTo("sample.jwt.token");
        assertThat(resultToken.expiresAt()).isEqualTo(expiresAt);

        verify(userRepository).findByEmail(email);
        verify(hasher).matches(rawPassword, storedHash);
        verify(tokenIssuePort).issue(user);
    }

    @Test
    @DisplayName("Test 2: Falla con InvalidCredentialException cuando el email no está registrado")
    void loginShouldThrowWhenUserNotFound() {
        // Arrange
        String email = "unknown@example.com";
        LoginCommand command = new LoginCommand(email, "AnyPassword123!");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(command))
                .isInstanceOf(InvalidCredentialException.class)
                .hasMessageContaining("Credenciales");

        verify(userRepository).findByEmail(email);
        verify(hasher, never()).matches(any(), any());
        verify(tokenIssuePort, never()).issue(any());
    }

    @Test
    @DisplayName("Test 3: Falla con InvalidCredentialException cuando la contraseña no coincide")
    void loginShouldThrowWhenPasswordDoesNotMatch() {
        // Arrange
        String email = "john.doe@example.com";
        String wrongPassword = "WrongPassword123!";
        String storedHash = "$2a$10$exampleHashedPasswordValueForTest123456789";
        User user = createSampleUser(email, storedHash);

        LoginCommand command = new LoginCommand(email, wrongPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches(wrongPassword, storedHash)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(command))
                .isInstanceOf(InvalidCredentialException.class)
                .hasMessageContaining("Credenciales");

        verify(userRepository).findByEmail(email);
        verify(hasher).matches(wrongPassword, storedHash);
        verify(tokenIssuePort, never()).issue(any());
    }

    @Test
    @DisplayName("Test 4: Comprueba que se envía el hash real (value) y NO '[PROTECTED]' al hasher")
    void loginShouldUseActualHashValueInsteadOfProtectedToString() {
        // Arrange
        String email = "security@example.com";
        String rawPassword = "SecretPassword123!";
        String realHash = "$2a$10$realSecretBcryptHashValueExample123456";
        User user = createSampleUser(email, realHash);

        LoginCommand command = new LoginCommand(email, rawPassword);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches(rawPassword, realHash)).thenReturn(true);
        when(tokenIssuePort.issue(user)).thenReturn(new AuthToken("jwt", Instant.now().plusSeconds(3600)));

        // Act
        loginService.login(command);

        // Assert: Garantiza que hasher nunca recibió "[PROTECTED]"
        verify(hasher).matches(eq(rawPassword), eq(realHash));
        verify(hasher, never()).matches(any(), eq("[PROTECTED]"));
    }
}
