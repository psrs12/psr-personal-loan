package com.personalloan.pricingorchestration.domain.pricing;

import java.time.LocalDateTime;
import java.util.UUID;

public class OfferSelection {

    private final UUID id;
    private final UUID applicationId;
    private final UUID selectedPricingOfferId;
    private final LocalDateTime offerSelectedTimestamp;

    private OfferSelection(UUID id, UUID applicationId, UUID selectedPricingOfferId,
                           LocalDateTime offerSelectedTimestamp) {
        this.id = id;
        this.applicationId = applicationId;
        this.selectedPricingOfferId = selectedPricingOfferId;
        this.offerSelectedTimestamp = offerSelectedTimestamp;
    }

    public static OfferSelection create(UUID applicationId, UUID selectedPricingOfferId) {
        return new OfferSelection(UUID.randomUUID(), applicationId, selectedPricingOfferId, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public UUID getApplicationId() { return applicationId; }
    public UUID getSelectedPricingOfferId() { return selectedPricingOfferId; }
    public LocalDateTime getOfferSelectedTimestamp() { return offerSelectedTimestamp; }
}
