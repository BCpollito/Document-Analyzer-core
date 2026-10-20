package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.application.port.in.command.LoginCommand;

public interface LoginUseCase {

    AuthToken login(LoginCommand command);
}
