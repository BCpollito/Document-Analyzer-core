package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.command.RegisterUserCommand;
import com.portfolio.documentanalyzer.application.port.out.PasswordEncoderPort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;
import com.portfolio.documentanalyzer.domain.exception.UserAlreadyExistsException;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    @DisplayName("Test 1: Registro exitoso codifica contraseña, guarda con hash y retorna UUID válido (CA-01, CA-04, CA-06)")
    void registerShouldSucceedWhenDataIsValid() {
        // Arrange
        String rawPassword = "SecurePassword123!";
        String passwordHash = "$2a$10$hashedPasswordExampleForTestOnly";
        RegisterUserCommand command = new RegisterUserCommand("johndoe", "john.doe@example.com", rawPassword);

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(passwordHash);

        UUID expectedId = UUID.randomUUID();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            return User.reconstitute(
                    new UserId(expectedId),
                    userArg.getUsername(),
                    userArg.getEmail(),
                    userArg.getPasswordHash(),
                    userArg.getCreatedAt()
            );
        });

        // Act
        UUID resultId = registerUserService.register(command);

        // Assert
        assertThat(resultId).isEqualTo(expectedId);

        // Verifica que se codificó la contraseña
        verify(passwordEncoder).encode(rawPassword);

        // Verifica que se guardó en el repositorio con el hash y nunca con la contraseña en texto plano
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername().value()).isEqualTo("johndoe");
        assertThat(capturedUser.getEmail().value()).isEqualTo("john.doe@example.com");
        assertThat(capturedUser.getPasswordHash().value()).isEqualTo(passwordHash);
        assertThat(capturedUser.getPasswordHash().value()).doesNotContain(rawPassword);
        assertThat(capturedUser.getId().value()).isNotNull();
    }

    @Test
    @DisplayName("Test 2: Fallo por email duplicado lanza UserAlreadyExistsException sin codificar ni guardar (CA-02, CA-05)")
    void registerShouldThrowWhenEmailAlreadyExists() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("johndoe", "existing@example.com", "SecurePassword123!");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registerUserService.register(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("El email ya está registrado");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",       // Menos de 8 caracteres
            "nouppercase123!", // Sin mayúscula
            "NOLOWERCASENUM!", // Sin número
            "NoSpecialChar123", // Sin carácter especial
            "        "        // En blanco
    })
    @DisplayName("Test 3: Fallo por política de contraseña lanza InvalidUserRegistrationException antes de persistir (CA-03, CA-05)")
    void registerShouldThrowWhenPasswordDoesNotMeetPolicy(String invalidPassword) {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("johndoe", "valid.email@example.com", invalidPassword);
        when(userRepository.existsByEmail("valid.email@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> registerUserService.register(command))
                .isInstanceOf(InvalidUserRegistrationException.class)
                .hasMessageContaining("La contraseña debe tener al menos 8 caracteres");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Fallo por username duplicado lanza UserAlreadyExistsException (CA-02, CA-05)")
    void registerShouldThrowWhenUsernameAlreadyExists() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("existing_user", "new.email@example.com", "SecurePassword123!");
        when(userRepository.existsByEmail("new.email@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registerUserService.register(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("El username ya está en uso");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
