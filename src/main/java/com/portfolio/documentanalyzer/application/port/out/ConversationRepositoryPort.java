package com.portfolio.documentanalyzer.application.port.out;

import com.portfolio.documentanalyzer.domain.model.Conversation;
import com.portfolio.documentanalyzer.domain.vo.ConversationId;
import com.portfolio.documentanalyzer.domain.vo.UserId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepositoryPort {
    Optional<Conversation> findByIdAndUserId(ConversationId conversationId, UserId userId);


    List<Conversation> findAllByUserId(UserId userId);


    boolean existsByIdAndUserId(ConversationId conversationId, UserId userId);
}
