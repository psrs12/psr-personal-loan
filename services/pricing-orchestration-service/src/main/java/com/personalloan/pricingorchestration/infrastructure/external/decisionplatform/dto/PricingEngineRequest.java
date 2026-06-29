package com.personalloan.pricingorchestration.infrastructure.external.decisionplatform.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PricingEngineRequest(
        UUID applicationId,
        String softPullCreditReportReferenceId,
        BigDecimal requestedAmount,
        Integer requestedTermMonths,
        String loanPurpose,
        BigDecimal annualIncome,
        String employmentStatus,
        String campaignOfferId,
        String campaignOfferTerms
) {}
