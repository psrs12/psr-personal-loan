package com.personalloan.pricingorchestration.infrastructure.external.decisionplatform.dto;

import java.util.UUID;

public record FinalDecisionRequest(
        UUID applicationId,
        UUID selectedPricingOfferId,
        String hardPullCreditReportReferenceId
) {}
