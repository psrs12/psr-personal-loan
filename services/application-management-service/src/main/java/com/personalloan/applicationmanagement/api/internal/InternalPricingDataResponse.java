package com.personalloan.applicationmanagement.api.internal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InternalPricingDataResponse(
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
