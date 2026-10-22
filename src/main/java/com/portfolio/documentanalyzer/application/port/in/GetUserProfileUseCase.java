package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.domain.model.User;

public interface GetUserProfileUseCase {
    User getUserProfile(String userId);
}
