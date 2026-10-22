package com.portfolio.documentanalyzer.application.port.out;

public interface PasswordHasherPort {
    boolean matches(String raw, String hash);
}
