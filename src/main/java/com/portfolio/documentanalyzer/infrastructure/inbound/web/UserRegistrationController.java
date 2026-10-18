package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.application.port.in.RegisterUserUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.RegisterUserCommand;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.ErrorResponseDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UserRegistrationRequestDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UserRegistrationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador de entrada REST para el registro de nuevos usuarios en el sistema.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Registration", description = "Endpoints para la gestión y registro de usuarios")
public class UserRegistrationController {

    private final RegisterUserUseCase registerUserUseCase;

    public UserRegistrationController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = Objects.requireNonNull(registerUserUseCase, "registerUserUseCase no puede ser nulo");
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Valida las credenciales de entrada, codifica la contraseña mediante BCrypt, persiste al usuario y retorna su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRegistrationResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o política de contraseña no satisfecha",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: el email o username ya se encuentra registrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    public ResponseEntity<UserRegistrationResponseDto> register(@Valid @RequestBody UserRegistrationRequestDto request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.username(),
                request.email(),
                request.password()
        );

        UUID userId = registerUserUseCase.register(command);

        UserRegistrationResponseDto response = UserRegistrationResponseDto.of(
                userId,
                request.username(),
                request.email()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
