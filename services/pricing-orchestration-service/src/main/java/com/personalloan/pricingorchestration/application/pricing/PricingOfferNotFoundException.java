package com.personalloan.pricingorchestration.application.pricing;

import java.util.UUID;

public class PricingOfferNotFoundException extends RuntimeException {
    public PricingOfferNotFoundException(UUID pricingOfferId) {
        super("Pricing offer not found: " + pricingOfferId);
    }
}
