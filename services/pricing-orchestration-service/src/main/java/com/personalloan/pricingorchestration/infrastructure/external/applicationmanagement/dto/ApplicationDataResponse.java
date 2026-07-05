package com.personalloan.pricingorchestration.infrastructure.external.applicationmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApplicationDataResponse(
        String softPullCreditReportReferenceId,
        BigDecimal requestedAmount,
        Integer requestedTermMonths,
        String loanPurpose,
        BigDecimal annualIncome,
        String employmentStatus,
        String campaignOfferId,
        String campaignOfferTerms,
        LocalDateTime applicationExpiryDate,
        String applicationStatus
) {}
