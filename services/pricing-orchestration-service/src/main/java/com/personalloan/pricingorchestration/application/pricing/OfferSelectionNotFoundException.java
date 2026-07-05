package com.personalloan.pricingorchestration.application.pricing;

import java.util.UUID;

public class OfferSelectionNotFoundException extends RuntimeException {
    public OfferSelectionNotFoundException(UUID applicationId) {
        super("No offer selection found for application: " + applicationId);
    }
}
