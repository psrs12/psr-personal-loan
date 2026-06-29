package com.personalloan.pricingorchestration.infrastructure.external.decisionplatform.dto;

import java.util.List;

public record FinalDecisionApiResponse(
        String outcome,
        String reasonCode,
        List<DocumentCodeDto> documents
) {
    public record DocumentCodeDto(String decisionEngineCode, int count) {}
}
