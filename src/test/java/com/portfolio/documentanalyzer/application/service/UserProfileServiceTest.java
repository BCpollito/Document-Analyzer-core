package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.command.UpdateUserProfileCommand;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.UserAlreadyExistsException;
import com.portfolio.documentanalyzer.domain.vo.Email;
import com.portfolio.documentanalyzer.domain.vo.PasswordHash;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import com.portfolio.documentanalyzer.domain.vo.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    @DisplayName("testUpdateUserProfile_Success: actualiza username y email y guarda en el repositorio")
    void testUpdateUserProfile_Success() {
        // Arrange
        UUID userIdUuid = UUID.randomUUID();
        UserId userId = UserId.of(userIdUuid);
        User existingUser = User.reconstitute(
                userId,
                new Username("old_username"),
                new Email("old.email@example.com"),
                new PasswordHash("$2a$10$hashedPassword123456789012345678901234567890123456789012"),
                Instant.now()
        );

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail(new Email("new.email@example.com"))).thenReturn(false);

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userIdUuid.toString(),
                "new_username",
                "new.email@example.com"
        );

        // Act
        User updatedUser = userProfileService.updateUserProfile(command);

        // Assert
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername().value()).isEqualTo("new_username");
        assertThat(updatedUser.getEmail().value()).isEqualTo("new.email@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername().value()).isEqualTo("new_username");
        assertThat(savedUser.getEmail().value()).isEqualTo("new.email@example.com");
    }

    @Test
    @DisplayName("testUpdateUserProfile_EmailAlreadyInUse: lanza UserAlreadyExistsException cuando el email ya está en uso")
    void testUpdateUserProfile_EmailAlreadyInUse() {
        // Arrange
        UUID userIdUuid = UUID.randomUUID();
        UserId userId = UserId.of(userIdUuid);
        User existingUser = User.reconstitute(
                userId,
                new Username("current_user"),
                new Email("current.email@example.com"),
                new PasswordHash("$2a$10$hashedPassword123456789012345678901234567890123456789012"),
                Instant.now()
        );

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail(new Email("existing.email@example.com"))).thenReturn(true);

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userIdUuid.toString(),
                "new_user",
                "existing.email@example.com"
        );

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userProfileService.updateUserProfile(command));

        verify(userRepositoryPort, never()).save(any(User.class));
    }
}
