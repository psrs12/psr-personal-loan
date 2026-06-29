package com.personalloan.pricingorchestration.infrastructure.external.decisionplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PricingEngineApiResponse(
        String outcome,
        List<OfferItem> offers,
        String declineReasonCode
) {
    public record OfferItem(
            String pricingOfferId,
            BigDecimal approvedAmount,
            BigDecimal interestRate,
            BigDecimal apr,
            int termMonths,
            BigDecimal monthlyRepayment,
            BigDecimal totalRepayable,
            LocalDateTime offerExpiryDate,
            String pricingModelRef,
            String bureauSnapshotRef
    ) {}
}
