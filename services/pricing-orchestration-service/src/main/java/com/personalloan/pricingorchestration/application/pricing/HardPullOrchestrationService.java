package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.FinalDecisionResponse;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.CreditManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.DecisionPlatformPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HardPullOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(HardPullOrchestrationService.class);

    private final CreditManagementPort creditManagementPort;
    private final ApplicationManagementPort applicationManagementPort;
    private final DecisionPlatformPort decisionPlatformPort;
    private final PricingEventPublisher eventPublisher;

    public HardPullOrchestrationService(CreditManagementPort creditManagementPort,
                                         ApplicationManagementPort applicationManagementPort,
                                         DecisionPlatformPort decisionPlatformPort,
                                         PricingEventPublisher eventPublisher) {
        this.creditManagementPort = creditManagementPort;
        this.applicationManagementPort = applicationManagementPort;
        this.decisionPlatformPort = decisionPlatformPort;
        this.eventPublisher = eventPublisher;
    }

    public void initiateHardPull(UUID applicationId, UUID selectedPricingOfferId, String applicantReference) {
        applicationManagementPort.updateApplicationStatus(applicationId, "HARD_PULL_PENDING");
        eventPublisher.publishHardPullInitiated(applicationId);

        try {
            PricingRequest assembled = applicationManagementPort.assembleFromApplicationData(applicationId);
            String hardPullReferenceId = creditManagementPort.initiateHardPull(
                    applicationId, applicantReference, assembled.softPullCreditReportReferenceId());

            applicationManagementPort.persistHardPullReference(applicationId, hardPullReferenceId);
            applicationManagementPort.updateApplicationStatus(applicationId, "DECISION_PENDING");
            eventPublisher.publishHardPullCompleted(applicationId, hardPullReferenceId);

            requestFinalDecision(applicationId, selectedPricingOfferId, hardPullReferenceId);
        } catch (Exception e) {
            log.error("Hard pull failed for application {}", applicationId, e);
            applicationManagementPort.updateApplicationStatus(applicationId, "DECLINED");
            eventPublisher.publishHardPullFailed(applicationId, e.getMessage());
        }
    }

    private void requestFinalDecision(UUID applicationId, UUID selectedPricingOfferId, String hardPullReferenceId) {
        FinalDecisionResponse decision = decisionPlatformPort.requestFinalDecision(
                applicationId, selectedPricingOfferId, hardPullReferenceId);

        switch (decision.outcome()) {
            case APPROVED -> {
                applicationManagementPort.updateApplicationStatus(applicationId, "APPROVED");
                eventPublisher.publishFinalDecisionApproved(applicationId);
            }
            case DECLINED -> {
                applicationManagementPort.updateApplicationStatus(applicationId, "DECLINED");
                eventPublisher.publishFinalDecisionDeclined(applicationId, decision.reasonCode());
            }
            case REFERRED -> {
                applicationManagementPort.updateApplicationStatus(applicationId, "REFERRED");
                eventPublisher.publishFinalDecisionReferred(applicationId);
            }
            case DOCUMENTS_REQUIRED -> {
                applicationManagementPort.updateApplicationStatus(applicationId, "DOCUMENTS_REQUIRED");
                eventPublisher.publishFinalDecisionDocumentsRequired(applicationId, decision.documents());
            }
        }
    }
}
