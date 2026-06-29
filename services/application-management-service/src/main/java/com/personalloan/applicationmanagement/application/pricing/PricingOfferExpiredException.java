package com.personalloan.applicationmanagement.application.pricing;

import java.util.UUID;

public class PricingOfferExpiredException extends RuntimeException {
    public PricingOfferExpiredException(UUID pricingOfferId) {
        super("Pricing offer has expired: " + pricingOfferId);
    }
}
