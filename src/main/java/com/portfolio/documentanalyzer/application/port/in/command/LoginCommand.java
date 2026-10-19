package com.portfolio.documentanalyzer.application.port.in.command;

public record LoginCommand(
        String email,
        String password
) {
}
