package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.PricingEngineResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.DecisionPlatformPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PricingRequestAssemblyService {

    private static final Logger log = LoggerFactory.getLogger(PricingRequestAssemblyService.class);

    private final ApplicationManagementPort applicationManagementPort;
    private final DecisionPlatformPort decisionPlatformPort;
    private final PricingEventPublisher eventPublisher;

    public PricingRequestAssemblyService(ApplicationManagementPort applicationManagementPort,
                                          DecisionPlatformPort decisionPlatformPort,
                                          PricingEventPublisher eventPublisher) {
        this.applicationManagementPort = applicationManagementPort;
        this.decisionPlatformPort = decisionPlatformPort;
        this.eventPublisher = eventPublisher;
    }

    public void requestPricing(UUID applicationId) {
        PricingRequest request = applicationManagementPort.assembleFromApplicationData(applicationId);
        PricingEngineResponse response = decisionPlatformPort.requestPricing(request);

        switch (response.outcome()) {
            case OFFERS_GENERATED -> {
                applicationManagementPort.persistPricingOffers(applicationId, response.offers());
                applicationManagementPort.updateApplicationStatus(applicationId, "OFFER_PENDING");
                eventPublisher.publishPricingOffersReceived(applicationId);
            }
            case DECLINED -> {
                log.info("Pricing declined for application {}: {}", applicationId, response.declineReasonCode());
                applicationManagementPort.updateApplicationStatus(applicationId, "DECLINED");
                eventPublisher.publishPricingDeclined(applicationId, response.declineReasonCode());
            }
        }
    }

    public void requestRePricing(UUID applicationId) {
        applicationManagementPort.markOffersSuperseded(applicationId);
        requestPricing(applicationId);
    }
}
