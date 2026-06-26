package com.personalloan.pricingorchestration.application.pricing;

import java.math.BigDecimal;
import java.util.UUID;

public record PricingRequest(
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
