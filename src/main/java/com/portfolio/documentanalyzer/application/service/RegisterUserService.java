package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.RegisterUserUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.RegisterUserCommand;
import com.portfolio.documentanalyzer.application.port.out.PasswordEncoderPort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;
import com.portfolio.documentanalyzer.domain.exception.UserAlreadyExistsException;
import com.portfolio.documentanalyzer.domain.model.User;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Caso de uso de registro de usuarios */
public class RegisterUserService implements RegisterUserUseCase {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public RegisterUserService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Override
    public UUID register(RegisterUserCommand command) {
        if (command == null || command.email() == null || command.email().isBlank()) {
            throw new InvalidUserRegistrationException("El email es obligatorio");
        }
        String email = command.email().strip().toLowerCase(Locale.ROOT);

        if (command.username() == null || command.username().isBlank()) {
            throw new InvalidUserRegistrationException("El username es obligatorio");
        }
        String username = command.username().strip();

        //email único
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }

        //username único
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("El username ya está en uso");
        }

        //política de contraseña
        validatePasswordPolicy(command.rawPassword());

        //solo se persiste el hash
        String passwordHash = passwordEncoder.encode(command.rawPassword());

        User user = User.createRegistration(username, email, passwordHash);
        return userRepository.save(user).getId();
    }

    private static void validatePasswordPolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH
                || rawPassword.chars().noneMatch(Character::isUpperCase)
                || rawPassword.chars().noneMatch(Character::isDigit)
                || rawPassword.chars().allMatch(c -> Character.isLetterOrDigit(c))) {
            throw new InvalidUserRegistrationException(
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial");
        }
    }
}
