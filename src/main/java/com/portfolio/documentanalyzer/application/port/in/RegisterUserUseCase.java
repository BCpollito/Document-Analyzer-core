package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.application.port.in.command.RegisterUserCommand;

import java.util.UUID;

/** Puerto de entrada: registro de usuarios. */
public interface RegisterUserUseCase {

    /**
     * @return el UUID del usuario creado
     */
    UUID register(RegisterUserCommand command);
}
