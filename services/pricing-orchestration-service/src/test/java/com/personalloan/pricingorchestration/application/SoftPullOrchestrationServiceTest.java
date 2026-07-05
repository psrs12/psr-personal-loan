package com.personalloan.pricingorchestration.application;

import com.personalloan.pricingorchestration.application.pricing.PricingRequestAssemblyService;
import com.personalloan.pricingorchestration.application.pricing.SoftPullOrchestrationService;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.CreditManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoftPullOrchestrationServiceTest {

    @Mock private CreditManagementPort creditManagementPort;
    @Mock private ApplicationManagementPort applicationManagementPort;
    @Mock private PricingEventPublisher eventPublisher;
    @Mock private PricingRequestAssemblyService pricingRequestAssemblyService;

    private SoftPullOrchestrationService service;

    @BeforeEach
    void setUp() {
        service = new SoftPullOrchestrationService(creditManagementPort, applicationManagementPort,
                eventPublisher, pricingRequestAssemblyService);
    }

    @Test
    void initiateSoftPull_success_persistsReferenceAndPublishesEvent() {
        UUID applicationId = UUID.randomUUID();
        String referenceId = "ref-123";
        when(creditManagementPort.initiateSoftPull(applicationId, null)).thenReturn(referenceId);

        service.initiateSoftPull(applicationId, null);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "SOFT_PULL_PENDING");
        verify(eventPublisher).publishSoftPullInitiated(applicationId);
        verify(applicationManagementPort).persistSoftPullReference(applicationId, referenceId);
        verify(applicationManagementPort).updateApplicationStatus(applicationId, "PRICING_PENDING");
        verify(eventPublisher).publishSoftPullCompleted(applicationId, referenceId);
        verify(pricingRequestAssemblyService).requestPricing(applicationId);
    }

    @Test
    void initiateSoftPull_creditManagementFails_leavesApplicationPendingAndPublishesFailedEvent() {
        UUID applicationId = UUID.randomUUID();
        when(creditManagementPort.initiateSoftPull(applicationId, null))
                .thenThrow(new RuntimeException("Credit Management unavailable"));

        service.initiateSoftPull(applicationId, null);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "SOFT_PULL_PENDING");
        verify(applicationManagementPort, never()).updateApplicationStatus(applicationId, "DECLINED");
        verify(eventPublisher).publishSoftPullFailed(eq(applicationId), anyString());
        verify(pricingRequestAssemblyService, never()).requestPricing(any());
    }
}
