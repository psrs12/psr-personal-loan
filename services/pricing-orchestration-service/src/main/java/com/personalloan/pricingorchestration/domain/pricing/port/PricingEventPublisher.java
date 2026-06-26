package com.personalloan.pricingorchestration.domain.pricing.port;

import java.util.UUID;

public interface PricingEventPublisher {
    void publishSoftPullInitiated(UUID applicationId);
    void publishSoftPullCompleted(UUID applicationId, String creditReportReferenceId);
    void publishSoftPullFailed(UUID applicationId, String reason);
    void publishPricingOffersReceived(UUID applicationId);
    void publishPricingDeclined(UUID applicationId, String reasonCode);
    void publishHardPullInitiated(UUID applicationId);
    void publishHardPullCompleted(UUID applicationId, String creditReportReferenceId);
    void publishHardPullFailed(UUID applicationId, String reason);
    void publishFinalDecisionApproved(UUID applicationId);
    void publishFinalDecisionDeclined(UUID applicationId, String reasonCode);
    void publishFinalDecisionReferred(UUID applicationId);
}
