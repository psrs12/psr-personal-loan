package com.personalloan.pricingorchestration.api.pricing;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SelectOfferRequest(
        @NotNull UUID selectedPricingOfferId
) {}
