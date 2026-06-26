package com.personalloan.applicationmanagement.infrastructure.persistence.pricing;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offer_selection")
public class OfferSelectionJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "selected_pricing_offer_id", nullable = false)
    private UUID selectedPricingOfferId;

    @Column(name = "offer_selected_timestamp", nullable = false)
    private LocalDateTime offerSelectedTimestamp;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getSelectedPricingOfferId() { return selectedPricingOfferId; }
    public void setSelectedPricingOfferId(UUID selectedPricingOfferId) { this.selectedPricingOfferId = selectedPricingOfferId; }
    public LocalDateTime getOfferSelectedTimestamp() { return offerSelectedTimestamp; }
    public void setOfferSelectedTimestamp(LocalDateTime offerSelectedTimestamp) { this.offerSelectedTimestamp = offerSelectedTimestamp; }
}
