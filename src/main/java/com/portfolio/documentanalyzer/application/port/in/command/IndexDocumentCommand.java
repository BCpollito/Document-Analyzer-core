package com.portfolio.documentanalyzer.application.port.in.command;

import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.vo.UserId;

import java.util.Map;

public record IndexDocumentCommand(
        String userId,
        String filename,
        String content
) {

    @Override
    public String toString() {
        return "IndexDocumentCommand[userId=" + userId
                + ", filename=" + filename
                + ", content=<" + (content == null ? 0 : content.length()) + " chars>]";
    }

}
