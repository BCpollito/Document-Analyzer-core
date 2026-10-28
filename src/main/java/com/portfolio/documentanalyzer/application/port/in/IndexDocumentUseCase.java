package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.application.port.in.command.IndexDocumentCommand;

import java.util.UUID;

public interface IndexDocumentUseCase {

    UUID index (IndexDocumentCommand indexDocumentCommand);
}
