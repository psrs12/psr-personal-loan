package com.personalloan.pricingorchestration.domain.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PricingEngineResponse(
        PricingOutcome outcome,
        List<PricingOfferData> offers,
        String declineReasonCode
) {
    public enum PricingOutcome { OFFERS_GENERATED, DECLINED }

    public record PricingOfferData(
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
