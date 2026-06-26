package com.personalloan.pricingorchestration.domain.pricing;

public record FinalDecisionResponse(
        DecisionOutcome outcome,
        String reasonCode
) {
    public enum DecisionOutcome { APPROVED, DECLINED, REFERRED }
}
