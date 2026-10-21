package com.portfolio.documentanalyzer.application.port.in.command;

public record UpdateUserProfileCommand (
        String userId, //se debe inyectar desde el token JWT, evitar usar el uso deñ body del request
        String userName,
        String email
){
}
