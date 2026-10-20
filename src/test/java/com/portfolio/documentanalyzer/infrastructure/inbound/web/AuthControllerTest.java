package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portfolio.documentanalyzer.application.port.in.LoginUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.application.port.in.command.LoginCommand;
import com.portfolio.documentanalyzer.domain.exception.InvalidCredentialException;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(loginUseCase);
        AuthController.AuthExceptionHandler authAdvice = controller.new AuthExceptionHandler();
        GlobalExceptionHandler globalAdvice = new GlobalExceptionHandler();

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(authAdvice, globalAdvice)
                .build();
    }

    @Test
    @DisplayName("Test 1: Login exitoso retorna HTTP 200 con token Bearer y fecha de expiración")
    void loginShouldReturn200AndTokenWhenCredentialsAreValid() throws Exception {
        LoginRequestDto request = new LoginRequestDto("john.doe@example.com", "SecurePassword123!");
        Instant expiresAt = Instant.now().plusSeconds(3600);
        AuthToken token = new AuthToken("sample.jwt.token", expiresAt);

        when(loginUseCase.login(any(LoginCommand.class))).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("sample.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists());

        verify(loginUseCase).login(new LoginCommand("john.doe@example.com", "SecurePassword123!"));
    }

    @Test
    @DisplayName("Test 2: Credenciales inválidas retornan HTTP 401 Unauthorized con mensaje de error")
    void loginShouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequestDto request = new LoginRequestDto("john.doe@example.com", "WrongPassword!");

        when(loginUseCase.login(any(LoginCommand.class)))
                .thenThrow(new InvalidCredentialException("Credenciales invalidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales invalidas"));

        verify(loginUseCase).login(any(LoginCommand.class));
    }

    @Test
    @DisplayName("Test 3: Email con formato inválido retorna HTTP 400 Bad Request")
    void loginShouldReturn400WhenEmailFormatIsInvalid() throws Exception {
        LoginRequestDto request = new LoginRequestDto("correo-invalido", "SecurePassword123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(loginUseCase, never()).login(any());
    }

    @Test
    @DisplayName("Test 4: Campos vacíos o nulos en el DTO retornan HTTP 400 Bad Request")
    void loginShouldReturn400WhenFieldsAreBlank() throws Exception {
        LoginRequestDto requestEmptyPassword = new LoginRequestDto("john.doe@example.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEmptyPassword)))
                .andExpect(status().isBadRequest());

        LoginRequestDto requestEmptyEmail = new LoginRequestDto("", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEmptyEmail)))
                .andExpect(status().isBadRequest());

        verify(loginUseCase, never()).login(any());
    }

    @Test
    @DisplayName("Test 5: Cuerpo de la solicitud vacío retorna HTTP 400 Bad Request")
    void loginShouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(loginUseCase, never()).login(any());
    }
}
