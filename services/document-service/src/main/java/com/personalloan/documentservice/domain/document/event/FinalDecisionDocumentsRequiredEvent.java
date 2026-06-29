package com.personalloan.documentservice.domain.document.event;

import java.util.List;
import java.util.UUID;

public record FinalDecisionDocumentsRequiredEvent(
        UUID applicationId,
        List<DocumentCodeEntry> documents
) {
    public record DocumentCodeEntry(String decisionEngineCode, int count) {}
}
