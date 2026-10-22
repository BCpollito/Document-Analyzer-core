package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.documentanalyzer.application.port.in.GetUserProfileUseCase;
import com.portfolio.documentanalyzer.application.port.in.UpdateUserProfileUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.UpdateUserProfileCommand;
import com.portfolio.documentanalyzer.domain.exception.UserAlreadyExistsException;
import com.portfolio.documentanalyzer.domain.model.Email;
import com.portfolio.documentanalyzer.domain.model.PasswordHash;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.model.UserId;
import com.portfolio.documentanalyzer.domain.model.Username;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UpdateUserProfileRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GetUserProfileUseCase getUserProfileUseCase;

    @Mock
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @BeforeEach
    void setUp() {
        UserProfileController controller = new UserProfileController(getUserProfileUseCase, updateUserProfileUseCase);
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("testGetProfile_Unauthorized: Solicitud GET sin autenticación (sin Principal) falla al requerir usuario autenticado")
    void testGetProfile_Unauthorized() {
        // Al no suministrar un Principal (sin token/autenticación), el endpoint no puede extraer el usuario
        assertThrows(Exception.class, () -> mockMvc.perform(get("/api/v1/user/me")));
    }

    @Test
    @DisplayName("testUpdateProfile_Success: PUT con Principal retorna 200 OK con UserProfileResponseDto")
    void testUpdateProfile_Success() throws Exception {
        UUID userIdUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Principal mockPrincipal = () -> userIdUuid.toString();
        UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto("updated_user", "updated@example.com");

        User updatedUser = User.reconstitute(
                UserId.of(userIdUuid),
                new Username("updated_user"),
                new Email("updated@example.com"),
                new PasswordHash("$2a$10$hashedPassword123456789012345678901234567890123456789012"),
                Instant.now()
        );

        when(updateUserProfileUseCase.updateUserProfile(any(UpdateUserProfileCommand.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/user/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userIdUuid.toString()))
                .andExpect(jsonPath("$.username").value("updated_user"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("testUpdateProfile_EmailInUse: PUT retorna 409 Conflict con código USER_ALREADY_EXISTS si el email está en uso")
    void testUpdateProfile_EmailInUse() throws Exception {
        UUID userIdUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Principal mockPrincipal = () -> userIdUuid.toString();
        UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto("updated_user", "duplicate@example.com");

        when(updateUserProfileUseCase.updateUserProfile(any(UpdateUserProfileCommand.class)))
                .thenThrow(new UserAlreadyExistsException("Email: duplicate@example.com ya se esta en uso"));

        mockMvc.perform(put("/api/v1/user/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.error.message").value("Email: duplicate@example.com ya se esta en uso"));
    }
}
