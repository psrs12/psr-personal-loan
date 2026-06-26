package com.personalloan.applicationmanagement.application.pricing;

import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import com.personalloan.applicationmanagement.domain.pricing.OfferSelection;
import com.personalloan.applicationmanagement.domain.pricing.PricingOffer;
import com.personalloan.applicationmanagement.domain.pricing.port.OfferSelectionRepository;
import com.personalloan.applicationmanagement.domain.pricing.port.PricingOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SelectOfferUseCase {

    private final ApplicationRepository applicationRepository;
    private final PricingOfferRepository pricingOfferRepository;
    private final OfferSelectionRepository offerSelectionRepository;

    public SelectOfferUseCase(ApplicationRepository applicationRepository,
                               PricingOfferRepository pricingOfferRepository,
                               OfferSelectionRepository offerSelectionRepository) {
        this.applicationRepository = applicationRepository;
        this.pricingOfferRepository = pricingOfferRepository;
        this.offerSelectionRepository = offerSelectionRepository;
    }

    @Transactional
    public OfferSelection execute(SelectOfferCommand command) {
        applicationRepository.findByApplicationId(command.applicationId())
                .orElseThrow(() -> new ApplicationNotFoundException(command.applicationId()));

        PricingOffer offer = pricingOfferRepository.findById(command.selectedPricingOfferId())
                .orElseThrow(() -> new PricingOfferNotFoundException(command.selectedPricingOfferId()));

        if (offer.isExpired()) {
            throw new PricingOfferExpiredException(command.selectedPricingOfferId());
        }

        OfferSelection selection = OfferSelection.create(command.applicationId(), command.selectedPricingOfferId());
        return offerSelectionRepository.save(selection);
    }
}
