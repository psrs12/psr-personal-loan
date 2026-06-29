package com.personalloan.pricingorchestration.domain.pricing;

import java.util.List;

public record FinalDecisionResponse(
        DecisionOutcome outcome,
        String reasonCode,
        List<DocumentCode> documents
) {
    public enum DecisionOutcome { APPROVED, DECLINED, REFERRED, DOCUMENTS_REQUIRED }

    public record DocumentCode(String decisionEngineCode, int count) {}
}
