package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.application.port.in.command.UpdateUserProfileCommand;

public interface UpdateUserProfileUseCase {
    User updateUserProfile(UpdateUserProfileCommand command);
}
