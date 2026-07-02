package com.personalloan.pricingorchestration.api.pricing;

import com.personalloan.pricingorchestration.domain.pricing.PricingOffer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PricingOfferResponse(
        UUID pricingOfferId,
        BigDecimal approvedAmount,
        BigDecimal interestRate,
        BigDecimal apr,
        int termMonths,
        BigDecimal monthlyRepayment,
        BigDecimal totalRepayable,
        LocalDateTime offerExpiryDate
) {
    public static PricingOfferResponse from(PricingOffer offer) {
        return new PricingOfferResponse(
                offer.getPricingOfferId(),
                offer.getApprovedAmount(),
                offer.getInterestRate(),
                offer.getApr(),
                offer.getTermMonths(),
                offer.getMonthlyRepayment(),
                offer.getTotalRepayable(),
                offer.getOfferExpiryDate());
    }
}
