package com.personalloan.pricingorchestration.application;

import com.personalloan.pricingorchestration.application.pricing.HardPullOrchestrationService;
import com.personalloan.pricingorchestration.domain.pricing.FinalDecisionResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.CreditManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.DecisionPlatformPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HardPullOrchestrationServiceTest {

    @Mock private CreditManagementPort creditManagementPort;
    @Mock private ApplicationManagementPort applicationManagementPort;
    @Mock private DecisionPlatformPort decisionPlatformPort;
    @Mock private PricingEventPublisher eventPublisher;

    private HardPullOrchestrationService service;

    private final UUID applicationId = UUID.randomUUID();
    private final UUID selectedOfferId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new HardPullOrchestrationService(creditManagementPort, applicationManagementPort,
                decisionPlatformPort, eventPublisher);
    }

    private void givenHardPullSucceeds(String referenceId) {
        PricingRequest assembled = new PricingRequest(applicationId, "soft-ref", BigDecimal.valueOf(10000),
                36, "DEBT_CONSOLIDATION", BigDecimal.valueOf(60000), "EMPLOYED", null, null);
        when(applicationManagementPort.assembleFromApplicationData(applicationId)).thenReturn(assembled);
        when(creditManagementPort.initiateHardPull(applicationId, null, "soft-ref")).thenReturn(referenceId);
    }

    @Test
    void initiateHardPull_approved_transitionsToApprovedAndPublishesEvent() {
        givenHardPullSucceeds("hard-ref-123");
        when(decisionPlatformPort.requestFinalDecision(applicationId, selectedOfferId, "hard-ref-123"))
                .thenReturn(new FinalDecisionResponse(FinalDecisionResponse.DecisionOutcome.APPROVED, null));

        service.initiateHardPull(applicationId, selectedOfferId, null);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "APPROVED");
        verify(eventPublisher).publishFinalDecisionApproved(applicationId);
    }

    @Test
    void initiateHardPull_declined_transitionsToDeclinedAndPublishesEvent() {
        givenHardPullSucceeds("hard-ref-456");
        when(decisionPlatformPort.requestFinalDecision(applicationId, selectedOfferId, "hard-ref-456"))
                .thenReturn(new FinalDecisionResponse(FinalDecisionResponse.DecisionOutcome.DECLINED, "HIGH_DTI"));

        service.initiateHardPull(applicationId, selectedOfferId, null);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "DECLINED");
        verify(eventPublisher).publishFinalDecisionDeclined(applicationId, "HIGH_DTI");
    }

    @Test
    void initiateHardPull_referred_transitionsToReferredAndPublishesEvent() {
        givenHardPullSucceeds("hard-ref-789");
        when(decisionPlatformPort.requestFinalDecision(applicationId, selectedOfferId, "hard-ref-789"))
                .thenReturn(new FinalDecisionResponse(FinalDecisionResponse.DecisionOutcome.REFERRED, null));

        service.initiateHardPull(applicationId, selectedOfferId, null);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "REFERRED");
        verify(eventPublisher).publishFinalDecisionReferred(applicationId);
    }

    @Test
    void initiateHardPull_creditManagementFails_declinesAndPublishesFailedEvent() {
        PricingRequest assembled = new PricingRequest(applicationId, "soft-ref", BigDecimal.valueOf(10000),
                36, "DEBT_CONSOLIDATION", BigDecimal.valueOf(60000), "EMPLOYED", null, null);
        when(applicationManagementPort.assembleFromApplicationData(applicationId)).thenReturn(assembled);
        when(creditManagementPort.initiateHardPull(any(), any(), any()))
                .thenThrow(new RuntimeException("Credit Management timeout"));

        service.initiateHardPull(applicationId, selectedOfferId, null);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "DECLINED");
        verify(eventPublisher).publishHardPullFailed(eq(applicationId), anyString());
        verify(decisionPlatformPort, never()).requestFinalDecision(any(), any(), any());
    }
}
