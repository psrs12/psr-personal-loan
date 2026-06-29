package com.personalloan.applicationmanagement.application.pricing;

import java.util.UUID;

public record CaptureConsentCommand(
        UUID applicationId,
        UUID selectedPricingOfferId,
        String consentChannel,
        String applicantReference
) {}
