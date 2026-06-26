package com.personalloan.applicationmanagement.domain.application;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;

import java.time.LocalDateTime;
import java.util.UUID;

public class Application {

    private final UUID applicationId;
    private final UUID intakeId;
    private final ApplicationSource applicationSource;
    private ApplicationStatus applicationStatus;
    private final LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
    private String softPullCreditReportReferenceId;
    private String hardPullCreditReportReferenceId;
    private String campaignOfferId;
    private String campaignOfferTerms;
    private LocalDateTime applicationExpiryDate;

    private Application(UUID applicationId, UUID intakeId, ApplicationSource applicationSource,
                         ApplicationStatus applicationStatus, LocalDateTime createdTimestamp) {
        this.applicationId = applicationId;
        this.intakeId = intakeId;
        this.applicationSource = applicationSource;
        this.applicationStatus = applicationStatus;
        this.createdTimestamp = createdTimestamp;
    }

    public static Application create(UUID intakeId, ApplicationSource source) {
        return new Application(UUID.randomUUID(), intakeId, source, ApplicationStatus.CREATED, LocalDateTime.now());
    }

    public static Application createDirect() {
        return new Application(UUID.randomUUID(), null, ApplicationSource.DIRECT, ApplicationStatus.CREATED, LocalDateTime.now());
    }

    public static Application reconstitute(UUID applicationId, UUID intakeId, ApplicationSource applicationSource,
                                            ApplicationStatus applicationStatus, LocalDateTime createdTimestamp,
                                            LocalDateTime updatedTimestamp, String softPullCreditReportReferenceId,
                                            String hardPullCreditReportReferenceId, String campaignOfferId,
                                            String campaignOfferTerms, LocalDateTime applicationExpiryDate) {
        Application app = new Application(applicationId, intakeId, applicationSource, applicationStatus, createdTimestamp);
        app.updatedTimestamp = updatedTimestamp;
        app.softPullCreditReportReferenceId = softPullCreditReportReferenceId;
        app.hardPullCreditReportReferenceId = hardPullCreditReportReferenceId;
        app.campaignOfferId = campaignOfferId;
        app.campaignOfferTerms = campaignOfferTerms;
        app.applicationExpiryDate = applicationExpiryDate;
        return app;
    }

    public void transitionTo(ApplicationStatus newStatus) {
        ApplicationStateMachine.validate(this.applicationStatus, newStatus);
        this.applicationStatus = newStatus;
        this.updatedTimestamp = LocalDateTime.now();
    }

    public void applySoftPullReference(String referenceId) {
        this.softPullCreditReportReferenceId = referenceId;
        this.updatedTimestamp = LocalDateTime.now();
    }

    public void applyHardPullReference(String referenceId) {
        this.hardPullCreditReportReferenceId = referenceId;
        this.updatedTimestamp = LocalDateTime.now();
    }

    public void applyCampaignOffer(String offerId, String offerTerms) {
        this.campaignOfferId = offerId;
        this.campaignOfferTerms = offerTerms;
        this.updatedTimestamp = LocalDateTime.now();
    }

    public boolean isExpired() {
        return applicationExpiryDate != null && LocalDateTime.now().isAfter(applicationExpiryDate);
    }

    public boolean isActive() {
        return applicationStatus == ApplicationStatus.CREATED
                || applicationStatus == ApplicationStatus.IN_PROGRESS
                || applicationStatus == ApplicationStatus.READY_FOR_SUBMISSION
                || applicationStatus == ApplicationStatus.SUBMITTED
                || applicationStatus == ApplicationStatus.PROCESSING
                || applicationStatus == ApplicationStatus.SOFT_PULL_PENDING
                || applicationStatus == ApplicationStatus.PRICING_PENDING
                || applicationStatus == ApplicationStatus.OFFER_PENDING
                || applicationStatus == ApplicationStatus.CONSENT_CAPTURED
                || applicationStatus == ApplicationStatus.HARD_PULL_PENDING
                || applicationStatus == ApplicationStatus.DECISION_PENDING;
    }

    public UUID getApplicationId() { return applicationId; }
    public UUID getIntakeId() { return intakeId; }
    public ApplicationSource getApplicationSource() { return applicationSource; }
    public ApplicationStatus getApplicationStatus() { return applicationStatus; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public String getSoftPullCreditReportReferenceId() { return softPullCreditReportReferenceId; }
    public String getHardPullCreditReportReferenceId() { return hardPullCreditReportReferenceId; }
    public String getCampaignOfferId() { return campaignOfferId; }
    public String getCampaignOfferTerms() { return campaignOfferTerms; }
    public LocalDateTime getApplicationExpiryDate() { return applicationExpiryDate; }
}
