package com.personalloan.applicationmanagement.application.pricing;

import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import com.personalloan.applicationmanagement.domain.pricing.PricingOffer;
import com.personalloan.applicationmanagement.domain.pricing.PricingOfferStatus;
import com.personalloan.applicationmanagement.domain.pricing.port.PricingOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetPricingOffersUseCase {

    private final ApplicationRepository applicationRepository;
    private final PricingOfferRepository pricingOfferRepository;
    private final ApplicationAuditRepository applicationAuditRepository;

    public GetPricingOffersUseCase(ApplicationRepository applicationRepository,
                                   PricingOfferRepository pricingOfferRepository,
                                   ApplicationAuditRepository applicationAuditRepository) {
        this.applicationRepository = applicationRepository;
        this.pricingOfferRepository = pricingOfferRepository;
        this.applicationAuditRepository = applicationAuditRepository;
    }

    @Transactional
    public List<PricingOffer> execute(UUID applicationId) {
        Application application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (application.isExpired()) {
            throw new ApplicationExpiredException(applicationId);
        }

        List<PricingOffer> offers = pricingOfferRepository.findByApplicationId(applicationId).stream()
                .filter(o -> PricingOfferStatus.ACTIVE.equals(o.getOfferStatus()))
                .filter(o -> !o.isExpired())
                .toList();

        applicationAuditRepository.save(ApplicationAuditRecord.of(
                applicationId, application.getIntakeId(),
                "PRICING_OFFERS_VIEWED",
                "{\"offerCount\":" + offers.size() + "}"
        ));

        return offers;
    }
}
