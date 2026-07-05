package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.OfferSelection;
import com.personalloan.pricingorchestration.domain.pricing.PricingOffer;
import com.personalloan.pricingorchestration.domain.pricing.port.OfferSelectionRepository;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingOfferRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetSelectedOfferUseCase {

    private final OfferSelectionRepository offerSelectionRepository;
    private final PricingOfferRepository pricingOfferRepository;

    public GetSelectedOfferUseCase(OfferSelectionRepository offerSelectionRepository,
                                    PricingOfferRepository pricingOfferRepository) {
        this.offerSelectionRepository = offerSelectionRepository;
        this.pricingOfferRepository = pricingOfferRepository;
    }

    public PricingOffer execute(UUID applicationId) {
        OfferSelection selection = offerSelectionRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new OfferSelectionNotFoundException(applicationId));

        return pricingOfferRepository.findById(selection.getSelectedPricingOfferId())
                .orElseThrow(() -> new PricingOfferNotFoundException(selection.getSelectedPricingOfferId()));
    }
}
