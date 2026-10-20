package com.portfolio.documentanalyzer.application.port.out;

import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.domain.model.User;

public interface TokenIssuePort {
    AuthToken issue(User user);
}
