package com.portfolio.documentanalyzer.infrastructure.outbound.security;

import com.portfolio.documentanalyzer.application.port.in.LoginUseCase;
import com.portfolio.documentanalyzer.application.port.out.PasswordHasherPort;
import com.portfolio.documentanalyzer.application.port.out.TokenIssuePort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.application.service.LoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    LoginUseCase loginUseCase(UserRepositoryPort userRepositoryPort, PasswordHasherPort hasher, TokenIssuePort issuePort){
        return new LoginService(userRepositoryPort, hasher, issuePort);
    }
}
