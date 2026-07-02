package com.personalloan.pricingorchestration.domain.pricing.port;

import com.personalloan.pricingorchestration.domain.pricing.ApplicationExpiryInfo;
import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;

import java.util.UUID;

public interface ApplicationManagementPort {
    PricingRequest assembleFromApplicationData(UUID applicationId);
    void updateApplicationStatus(UUID applicationId, String status);
    void persistSoftPullReference(UUID applicationId, String creditReportReferenceId);
    void persistHardPullReference(UUID applicationId, String creditReportReferenceId);
    ApplicationExpiryInfo getApplicationExpiryInfo(UUID applicationId);
    void recordAuditEvent(UUID applicationId, String eventType, String payload);
}
