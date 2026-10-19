package com.portfolio.documentanalyzer.application.port.out;

import com.portfolio.documentanalyzer.domain.model.PasswordHash;

public interface PasswordHasherPort {
    boolean matches(String raw, String hash);
}
