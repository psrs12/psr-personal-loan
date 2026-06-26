package com.personalloan.applicationmanagement.domain.pricing;

import java.time.LocalDateTime;
import java.util.UUID;

public class ConsentRecord {

    private final UUID id;
    private final UUID applicationId;
    private final ConsentType consentType;
    private final LocalDateTime consentGivenAt;
    private final String consentChannel;
    private final String applicantReference;
    private final UUID selectedPricingOfferId;

    private ConsentRecord(UUID id, UUID applicationId, ConsentType consentType,
                          LocalDateTime consentGivenAt, String consentChannel,
                          String applicantReference, UUID selectedPricingOfferId) {
        this.id = id;
        this.applicationId = applicationId;
        this.consentType = consentType;
        this.consentGivenAt = consentGivenAt;
        this.consentChannel = consentChannel;
        this.applicantReference = applicantReference;
        this.selectedPricingOfferId = selectedPricingOfferId;
    }

    public static ConsentRecord createHardPullConsent(UUID applicationId, String consentChannel,
                                                       String applicantReference) {
        return new ConsentRecord(UUID.randomUUID(), applicationId, ConsentType.HARD_PULL,
                LocalDateTime.now(), consentChannel, applicantReference, null);
    }

    public static ConsentRecord createOfferAcceptanceConsent(UUID applicationId, String consentChannel,
                                                              UUID selectedPricingOfferId) {
        return new ConsentRecord(UUID.randomUUID(), applicationId, ConsentType.OFFER_ACCEPTANCE,
                LocalDateTime.now(), consentChannel, null, selectedPricingOfferId);
    }

    public UUID getId() { return id; }
    public UUID getApplicationId() { return applicationId; }
    public ConsentType getConsentType() { return consentType; }
    public LocalDateTime getConsentGivenAt() { return consentGivenAt; }
    public String getConsentChannel() { return consentChannel; }
    public String getApplicantReference() { return applicantReference; }
    public UUID getSelectedPricingOfferId() { return selectedPricingOfferId; }
}
