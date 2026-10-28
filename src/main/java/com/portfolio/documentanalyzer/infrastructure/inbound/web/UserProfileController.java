package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.application.port.in.GetUserProfileUseCase;
import com.portfolio.documentanalyzer.application.port.in.UpdateUserProfileUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.UpdateUserProfileCommand;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.infrastructure.config.OpenApiConfig;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UserProfileResponseDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UpdateUserProfileRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping({"/api/v1/users/me", "/api/v1/user/me"})
@Tag(name = "User Profile", description = "Endpoints para la gestión de la cuenta personal")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class UserProfileController {
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;

    public UserProfileController(GetUserProfileUseCase getUserProfileUseCase, UpdateUserProfileUseCase updateUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
    }

    @Operation(summary = "Obtener perfil", description = "Retorna los datos del usuario autenticado basándose en su token.")
    @GetMapping
    public ResponseEntity<UserProfileResponseDto> getUserProfile(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        String userId = principal.getName();

        User user = getUserProfileUseCase.getUserProfile(userId);

        return ResponseEntity.ok(toDto(user));
    }

    @Operation(summary = "Actualizar perfil", description = "Modifica el username o email del usuario autenticado.")
    @PutMapping
    public ResponseEntity<UserProfileResponseDto> updateUserProfile(
            @RequestBody UpdateUserProfileRequestDto request,
            Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        String userId  = principal.getName();

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId,
                request.username(),
                request.email()
        );

        User updateUser = updateUserProfileUseCase.updateUserProfile(command);

        return ResponseEntity.ok(toDto(updateUser));
    }

    private UserProfileResponseDto toDto(User user) {
        return new UserProfileResponseDto(
          user.getId().toString(),
          user.getUsername().value(),
          user.getEmail().value()
        );
    }
}
