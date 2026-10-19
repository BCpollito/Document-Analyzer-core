package com.portfolio.documentanalyzer.application.port.out;

import com.portfolio.documentanalyzer.domain.model.User;

import java.util.Optional;

/** Puerto de salida para la persistencia de usuarios. */
public interface UserRepositoryPort {

    User save(User user);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);

}
