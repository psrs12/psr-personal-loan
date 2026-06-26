package com.personalloan.pricingorchestration.domain.pricing.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsentCapturedEvent(
        UUID applicationId,
        UUID selectedPricingOfferId,
        String applicantReference,
        LocalDateTime consentCapturedAt
) {}
