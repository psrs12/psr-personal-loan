package com.personalloan.pricingorchestration.domain.pricing.port;

import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;

import java.util.UUID;

public interface ApplicationManagementPort {
    PricingRequest assembleFromApplicationData(UUID applicationId);
    void updateApplicationStatus(UUID applicationId, String status);
    void persistSoftPullReference(UUID applicationId, String creditReportReferenceId);
    void persistHardPullReference(UUID applicationId, String creditReportReferenceId);
    void persistPricingOffers(UUID applicationId, Object offersPayload);
    void markOffersSuperseded(UUID applicationId);
}
