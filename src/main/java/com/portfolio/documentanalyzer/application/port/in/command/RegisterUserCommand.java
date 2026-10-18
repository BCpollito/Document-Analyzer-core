package com.portfolio.documentanalyzer.application.port.in.command;

/** Comando de registro de usuario. */
public record RegisterUserCommand(String username, String email, String rawPassword) {

    /** Evita filtrar la contraseña en texto plano en logs. */
    @Override
    public String toString() {
        return "RegisterUserCommand[username=" + username + ", email=" + email + ", rawPassword=***]";
    }
}
