package com.personalloan.pricingorchestration.infrastructure.persistence.pricing;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_record")
public class ConsentRecordJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "consent_type", nullable = false)
    private String consentType;

    @Column(name = "consent_given_at", nullable = false)
    private LocalDateTime consentGivenAt;

    @Column(name = "consent_channel", nullable = false)
    private String consentChannel;

    @Column(name = "applicant_reference")
    private String applicantReference;

    @Column(name = "selected_pricing_offer_id")
    private UUID selectedPricingOfferId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
    public LocalDateTime getConsentGivenAt() { return consentGivenAt; }
    public void setConsentGivenAt(LocalDateTime consentGivenAt) { this.consentGivenAt = consentGivenAt; }
    public String getConsentChannel() { return consentChannel; }
    public void setConsentChannel(String consentChannel) { this.consentChannel = consentChannel; }
    public String getApplicantReference() { return applicantReference; }
    public void setApplicantReference(String applicantReference) { this.applicantReference = applicantReference; }
    public UUID getSelectedPricingOfferId() { return selectedPricingOfferId; }
    public void setSelectedPricingOfferId(UUID selectedPricingOfferId) { this.selectedPricingOfferId = selectedPricingOfferId; }
}
