package com.portfolio.documentanalyzer.infrastructure.config;

import com.portfolio.documentanalyzer.application.port.in.RegisterUserUseCase;
import com.portfolio.documentanalyzer.application.port.out.PasswordEncoderPort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.application.service.RegisterUserService;
import com.portfolio.documentanalyzer.application.service.UserProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar los casos de uso como beans de Spring,
 * manteniendo la capa de aplicación libre de anotaciones del framework.
 */
@Configuration
public class UserUseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort
    ) {
        return new RegisterUserService(userRepositoryPort, passwordEncoderPort);
    }

    @Bean
    public UserProfileService userProfileService(UserRepositoryPort userRepositoryPort) {
        return new UserProfileService(userRepositoryPort);
    }

}
