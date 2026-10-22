package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.application.port.in.LoginUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.application.port.in.command.LoginCommand;
import com.portfolio.documentanalyzer.domain.exception.InvalidCredentialException;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.ErrorResponseDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.LoginRequestDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.LoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints para la autenticación y emisión de tokens")
public class AuthController {
    private final LoginUseCase loginUseCase;

    public AuthController (LoginUseCase loginUseCase){
        this.loginUseCase=loginUseCase;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica al usuario con email y contraseña, retornando un token JWT Bearer. Endpoint público."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa, token emitido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public LoginResponseDto login(@Valid @RequestBody  LoginRequestDto requestDto){
        AuthToken t = loginUseCase.login(new LoginCommand(requestDto.email(), requestDto.password()));
        return new LoginResponseDto(t.accesTokenm(), "Bearer", t.expiresAt());
    }

    @RestControllerAdvice
    class AuthExceptionHandler{
        @ExceptionHandler(InvalidCredentialException.class)
        ResponseEntity<Map<String,String>> invalid(){
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
        }
    }
}
