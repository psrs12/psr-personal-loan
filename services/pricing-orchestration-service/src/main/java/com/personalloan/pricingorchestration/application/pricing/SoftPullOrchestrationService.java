package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.CreditManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SoftPullOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(SoftPullOrchestrationService.class);

    private final CreditManagementPort creditManagementPort;
    private final ApplicationManagementPort applicationManagementPort;
    private final PricingEventPublisher eventPublisher;
    private final PricingRequestAssemblyService pricingRequestAssemblyService;

    public SoftPullOrchestrationService(CreditManagementPort creditManagementPort,
                                         ApplicationManagementPort applicationManagementPort,
                                         PricingEventPublisher eventPublisher,
                                         PricingRequestAssemblyService pricingRequestAssemblyService) {
        this.creditManagementPort = creditManagementPort;
        this.applicationManagementPort = applicationManagementPort;
        this.eventPublisher = eventPublisher;
        this.pricingRequestAssemblyService = pricingRequestAssemblyService;
    }

    public void initiateSoftPull(UUID applicationId, String applicantReference) {
        applicationManagementPort.updateApplicationStatus(applicationId, "SOFT_PULL_PENDING");
        eventPublisher.publishSoftPullInitiated(applicationId);

        try {
            String creditReportReferenceId = creditManagementPort.initiateSoftPull(applicationId, applicantReference);
            applicationManagementPort.persistSoftPullReference(applicationId, creditReportReferenceId);
            applicationManagementPort.updateApplicationStatus(applicationId, "PRICING_PENDING");
            eventPublisher.publishSoftPullCompleted(applicationId, creditReportReferenceId);

            pricingRequestAssemblyService.requestPricing(applicationId);
        } catch (Exception e) {
            log.error("Soft pull failed for application {}", applicationId, e);
            applicationManagementPort.updateApplicationStatus(applicationId, "DECLINED");
            eventPublisher.publishSoftPullFailed(applicationId, e.getMessage());
        }
    }
}
