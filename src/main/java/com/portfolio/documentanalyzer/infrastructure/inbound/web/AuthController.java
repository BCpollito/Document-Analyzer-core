package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.application.port.in.LoginUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.application.port.in.command.LoginCommand;
import com.portfolio.documentanalyzer.domain.exception.InvalidCredentialException;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.LoginRequestDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.LoginResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final LoginUseCase loginUseCase;

    public AuthController (LoginUseCase loginUseCase){
        this.loginUseCase=loginUseCase;
    }

    @PostMapping("/login")
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
