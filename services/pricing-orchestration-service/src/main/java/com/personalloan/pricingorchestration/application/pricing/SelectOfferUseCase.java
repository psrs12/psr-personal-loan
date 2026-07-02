package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.OfferSelection;
import com.personalloan.pricingorchestration.domain.pricing.PricingOffer;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.OfferSelectionRepository;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SelectOfferUseCase {

    private final PricingOfferRepository pricingOfferRepository;
    private final OfferSelectionRepository offerSelectionRepository;
    private final ApplicationManagementPort applicationManagementPort;

    public SelectOfferUseCase(PricingOfferRepository pricingOfferRepository,
                               OfferSelectionRepository offerSelectionRepository,
                               ApplicationManagementPort applicationManagementPort) {
        this.pricingOfferRepository = pricingOfferRepository;
        this.offerSelectionRepository = offerSelectionRepository;
        this.applicationManagementPort = applicationManagementPort;
    }

    @Transactional
    public OfferSelection execute(SelectOfferCommand command) {
        PricingOffer offer = pricingOfferRepository.findById(command.selectedPricingOfferId())
                .orElseThrow(() -> new PricingOfferNotFoundException(command.selectedPricingOfferId()));

        if (offer.isExpired()) {
            throw new PricingOfferExpiredException(command.selectedPricingOfferId());
        }

        OfferSelection selection = OfferSelection.create(command.applicationId(), command.selectedPricingOfferId());
        OfferSelection saved = offerSelectionRepository.save(selection);

        applicationManagementPort.recordAuditEvent(
                command.applicationId(),
                "OFFER_SELECTED",
                "{\"selectedPricingOfferId\":\"" + command.selectedPricingOfferId() + "\"}");

        return saved;
    }
}
