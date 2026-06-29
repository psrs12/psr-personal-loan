package com.personalloan.applicationmanagement.application.pricing;

import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
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
    private final ApplicationAuditRepository applicationAuditRepository;

    public SelectOfferUseCase(ApplicationRepository applicationRepository,
                               PricingOfferRepository pricingOfferRepository,
                               OfferSelectionRepository offerSelectionRepository,
                               ApplicationAuditRepository applicationAuditRepository) {
        this.applicationRepository = applicationRepository;
        this.pricingOfferRepository = pricingOfferRepository;
        this.offerSelectionRepository = offerSelectionRepository;
        this.applicationAuditRepository = applicationAuditRepository;
    }

    @Transactional
    public OfferSelection execute(SelectOfferCommand command) {
        var application = applicationRepository.findByApplicationId(command.applicationId())
                .orElseThrow(() -> new ApplicationNotFoundException(command.applicationId()));

        PricingOffer offer = pricingOfferRepository.findById(command.selectedPricingOfferId())
                .orElseThrow(() -> new PricingOfferNotFoundException(command.selectedPricingOfferId()));

        if (offer.isExpired()) {
            throw new PricingOfferExpiredException(command.selectedPricingOfferId());
        }

        OfferSelection selection = OfferSelection.create(command.applicationId(), command.selectedPricingOfferId());
        OfferSelection saved = offerSelectionRepository.save(selection);

        applicationAuditRepository.save(ApplicationAuditRecord.of(
                command.applicationId(), application.getIntakeId(),
                "OFFER_SELECTED",
                "{\"selectedPricingOfferId\":\"" + command.selectedPricingOfferId() + "\"}"
        ));

        return saved;
    }
}
