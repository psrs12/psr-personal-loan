package com.personalloan.pricingorchestration.api.pricing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CaptureConsentRequest(
        @NotNull UUID selectedPricingOfferId,
        @NotBlank String consentChannel,
        String applicantReference
) {}
