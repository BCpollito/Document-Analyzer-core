package com.portfolio.documentanalyzer.application.port.out;

import com.portfolio.documentanalyzer.domain.model.Conversation;
import com.portfolio.documentanalyzer.domain.vo.ConversationId;
import com.portfolio.documentanalyzer.domain.vo.UserId;

import java.util.List;
import java.util.Optional;

public interface ConversationRepositoryPort {
    Conversation save(Conversation conversation);
    Optional<Conversation> findByIdAndUserId(ConversationId conversationId, UserId userId);
    List<Conversation> findAllByUserId(UserId userId);
    boolean existsByIdAndUserId(ConversationId conversationId, UserId userId);
    void deleteByIdAndUserId(ConversationId conversationId, UserId userId);
}
