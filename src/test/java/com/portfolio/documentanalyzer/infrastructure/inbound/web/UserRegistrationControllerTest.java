package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.documentanalyzer.application.port.in.RegisterUserUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.RegisterUserCommand;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UserRegistrationRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRegistrationController.class)
@Import(GlobalExceptionHandler.class)
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @Test
    @DisplayName("Test 1: Petición HTTP POST válida retorna 201 Created sin exponer la contraseña ni su hash")
    void registerUserShouldReturn201CreatedWhenValid() throws Exception {
        UUID generatedId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "john_doe",
                "john.doe@example.com",
                "SecurePassword123!"
        );

        when(registerUserUseCase.register(any(RegisterUserCommand.class))).thenReturn(generatedId);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());

        verify(registerUserUseCase).register(any(RegisterUserCommand.class));
    }

    @Test
    @DisplayName("Test 2: Petición con email inválido retorna 400 Bad Request por validación del DTO (CA-05)")
    void registerUserShouldReturn400WhenEmailIsInvalid() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "john_doe",
                "correo-sin-arroba",
                "SecurePassword123!"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value(containsString("email")));

        verify(registerUserUseCase, never()).register(any());
    }

    @Test
    @DisplayName("Test 3: Petición sin body o con datos incompletos no procesa el registro (CA-06)")
    void registerUserShouldReturn400WhenBodyIsMissingOrIncomplete() throws Exception {
        // Caso A: Petición sin cuerpo
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        // Caso B: Petición con campos obligatorios vacíos o nulos
        UserRegistrationRequestDto incompleteRequest = new UserRegistrationRequestDto(
                "",
                "valid.email@example.com",
                ""
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(registerUserUseCase, never()).register(any());
    }
}
