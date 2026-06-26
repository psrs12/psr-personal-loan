package com.personalloan.applicationmanagement.application.pricing;

import java.util.UUID;

public record SelectOfferCommand(
        UUID applicationId,
        UUID selectedPricingOfferId
) {}
