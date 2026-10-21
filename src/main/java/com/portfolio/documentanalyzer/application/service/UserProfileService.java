package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.GetUserProfileUseCase;
import com.portfolio.documentanalyzer.application.port.in.UpdateUserProfileUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.UpdateUserProfileCommand;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.model.Email;
import com.portfolio.documentanalyzer.domain.model.UserId;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.model.Username;
import com.portfolio.documentanalyzer.domain.exception.UserAlreadyExistsException;

import java.util.UUID;

public class UserProfileService implements GetUserProfileUseCase, UpdateUserProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserProfileService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User getUserProfile(String userIdStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID de usuario debe ser un UUID válido.");
        }

        UserId userId = UserId.of(uuid);

        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    @Override
    public User updateUserProfile(UpdateUserProfileCommand command) {

        UUID uuid;
        try {
            uuid = UUID.fromString(command.userId());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID de usuario debe ser un UUID válido.");
        }

        UserId userId = UserId.of(uuid);
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Validar y convertir campos instanciando los Value Objects
        Username newUsername = command.userName() != null && !command.userName().isBlank()
                ? new Username(command.userName()) : null;

        Email newEmail = null;
        if (command.email() != null && !command.email().isBlank()) {
            newEmail = new Email(command.email());

            if (!user.getEmail().value().equals(newEmail.value()) && userRepositoryPort.existsByEmail(newEmail)) {
                throw new UserAlreadyExistsException("Email: "+ newEmail.value() + " ya se esta en uso");
            }
        }

        user.updateProfile(newUsername, newEmail);

        userRepositoryPort.save(user);

        return user;
    }

}
