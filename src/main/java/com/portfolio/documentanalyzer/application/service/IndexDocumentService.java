package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.IndexDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.command.IndexDocumentCommand;
import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.ResorceNotFoundException;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.vo.UserId;

import java.util.UUID;

public class IndexDocumentService implements IndexDocumentUseCase {

    private final DocumentRepositoryPort documentRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public IndexDocumentService(DocumentRepositoryPort documentRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.documentRepositoryPort = documentRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UUID index (IndexDocumentCommand indexDocumentCommand){
        UUID uuiUserid = UUID.fromString(indexDocumentCommand.userId());
        UserId userId = UserId.of(uuiUserid);

        User user = userRepositoryPort.findById(userId).orElseThrow(() -> new ResorceNotFoundException("Usuaurio no encontrado"));

        Document document= Document.create(userId, indexDocumentCommand.filename(),indexDocumentCommand.filename());

        return documentRepositoryPort.save(document, userId).getUserId().value();
    }
}
