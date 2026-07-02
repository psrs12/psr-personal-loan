package com.personalloan.pricingorchestration.application.pricing;

import java.util.UUID;

public record SelectOfferCommand(
        UUID applicationId,
        UUID selectedPricingOfferId
) {}
