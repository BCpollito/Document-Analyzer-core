package com.portfolio.documentanalyzer.application.port.out;

import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.vo.Email;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import com.portfolio.documentanalyzer.domain.vo.Username;

import java.util.Optional;

/** Puerto de salida para la persistencia de usuarios. */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UserId userId);

    boolean existsByEmail(Email email);

    boolean existsByUsername(Username username);

    Optional<User> findByEmail(Email email);

}
