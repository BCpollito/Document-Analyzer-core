package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.LoginUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.application.port.in.command.LoginCommand;
import com.portfolio.documentanalyzer.application.port.out.PasswordHasherPort;
import com.portfolio.documentanalyzer.application.port.out.TokenIssuePort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.InvalidCredentialException;
import com.portfolio.documentanalyzer.domain.model.User;

public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort hasher;
    private final TokenIssuePort tokenIssuePort;

    public LoginService(UserRepositoryPort userRepositoryPort, PasswordHasherPort hasher,
                        TokenIssuePort tokenIssuePort){
        this.userRepositoryPort=userRepositoryPort;
        this.tokenIssuePort=tokenIssuePort;
        this.hasher =hasher;
    }

    @Override
    public AuthToken login (LoginCommand command){
        User user = userRepositoryPort.findByEmail(command.email())
                .filter(u -> hasher.matches(command.password(), u.getPasswordHash().value()))
                .orElseThrow(() -> new InvalidCredentialException("Credenciales invalidas"));
        return tokenIssuePort.issue(user);
    }

}
