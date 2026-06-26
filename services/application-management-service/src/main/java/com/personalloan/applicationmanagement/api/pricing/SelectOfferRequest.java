package com.personalloan.applicationmanagement.api.pricing;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SelectOfferRequest(
        @NotNull UUID selectedPricingOfferId
) {}
