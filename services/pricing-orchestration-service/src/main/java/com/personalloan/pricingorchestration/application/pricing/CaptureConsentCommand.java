package com.personalloan.pricingorchestration.application.pricing;

import java.util.UUID;

public record CaptureConsentCommand(
        UUID applicationId,
        UUID selectedPricingOfferId,
        String consentChannel,
        String applicantReference
) {}
