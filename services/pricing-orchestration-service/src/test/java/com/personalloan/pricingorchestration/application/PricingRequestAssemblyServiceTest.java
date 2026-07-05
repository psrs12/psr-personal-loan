package com.personalloan.pricingorchestration.application;

import com.personalloan.pricingorchestration.application.pricing.PricingRequestAssemblyService;
import com.personalloan.pricingorchestration.domain.pricing.PricingEngineResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.DecisionPlatformPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingRequestAssemblyServiceTest {

    @Mock private ApplicationManagementPort applicationManagementPort;
    @Mock private DecisionPlatformPort decisionPlatformPort;
    @Mock private PricingEventPublisher eventPublisher;
    @Mock private PricingOfferRepository pricingOfferRepository;

    private PricingRequestAssemblyService service;

    @BeforeEach
    void setUp() {
        service = new PricingRequestAssemblyService(applicationManagementPort, decisionPlatformPort, eventPublisher, pricingOfferRepository);
    }

    @Test
    void requestPricing_offersGenerated_persistsOffersAndTransitionsToOfferPending() {
        UUID applicationId = UUID.randomUUID();
        PricingRequest request = new PricingRequest(applicationId, "ref-123",
                BigDecimal.valueOf(10000), 36, "DEBT_CONSOLIDATION",
                BigDecimal.valueOf(60000), "EMPLOYED", null, null);

        PricingEngineResponse.PricingOfferData offer = new PricingEngineResponse.PricingOfferData(
                UUID.randomUUID().toString(), BigDecimal.valueOf(10000),
                BigDecimal.valueOf(0.0599), BigDecimal.valueOf(0.0620),
                36, BigDecimal.valueOf(303.50), BigDecimal.valueOf(10926),
                LocalDateTime.now().plusDays(7), "model-v1", "bureau-snap-1");

        PricingEngineResponse response = new PricingEngineResponse(
                PricingEngineResponse.PricingOutcome.OFFERS_GENERATED, List.of(offer), null);

        when(applicationManagementPort.assembleFromApplicationData(applicationId)).thenReturn(request);
        when(decisionPlatformPort.requestPricing(request)).thenReturn(response);

        service.requestPricing(applicationId);

        verify(pricingOfferRepository).saveAll(any());
        verify(applicationManagementPort).updateApplicationStatus(applicationId, "OFFER_PENDING");
        verify(eventPublisher).publishPricingOffersReceived(applicationId);
    }

    @Test
    void requestPricing_declined_declinesApplicationAndPublishesEvent() {
        UUID applicationId = UUID.randomUUID();
        PricingRequest request = new PricingRequest(applicationId, "ref-456",
                BigDecimal.valueOf(5000), 24, "HOME_IMPROVEMENT",
                BigDecimal.valueOf(30000), "SELF_EMPLOYED", "offer-99", null);

        PricingEngineResponse response = new PricingEngineResponse(
                PricingEngineResponse.PricingOutcome.DECLINED, List.of(), "CREDIT_SCORE_TOO_LOW");

        when(applicationManagementPort.assembleFromApplicationData(applicationId)).thenReturn(request);
        when(decisionPlatformPort.requestPricing(request)).thenReturn(response);

        service.requestPricing(applicationId);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "DECLINED");
        verify(eventPublisher).publishPricingDeclined(applicationId, "CREDIT_SCORE_TOO_LOW");
    }

    @Test
    void requestRePricing_supersedePreviousOffersAndRequestNewPricing() {
        UUID applicationId = UUID.randomUUID();
        PricingRequest request = new PricingRequest(applicationId, "ref-789",
                BigDecimal.valueOf(8000), 48, "VEHICLE", BigDecimal.valueOf(45000), "EMPLOYED", null, null);

        PricingEngineResponse response = new PricingEngineResponse(
                PricingEngineResponse.PricingOutcome.OFFERS_GENERATED, List.of(), null);

        when(pricingOfferRepository.findByApplicationId(applicationId)).thenReturn(List.of());
        when(applicationManagementPort.assembleFromApplicationData(applicationId)).thenReturn(request);
        when(decisionPlatformPort.requestPricing(request)).thenReturn(response);

        service.requestRePricing(applicationId);

        verify(pricingOfferRepository).findByApplicationId(applicationId);
        verify(pricingOfferRepository).saveAll(List.of());
        verify(decisionPlatformPort).requestPricing(request);
    }
}
