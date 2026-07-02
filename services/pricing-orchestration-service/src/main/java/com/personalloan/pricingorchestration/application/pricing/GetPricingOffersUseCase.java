package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.ApplicationExpiryInfo;
import com.personalloan.pricingorchestration.domain.pricing.PricingOffer;
import com.personalloan.pricingorchestration.domain.pricing.PricingOfferStatus;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetPricingOffersUseCase {

    private final PricingOfferRepository pricingOfferRepository;
    private final ApplicationManagementPort applicationManagementPort;

    public GetPricingOffersUseCase(PricingOfferRepository pricingOfferRepository,
                                    ApplicationManagementPort applicationManagementPort) {
        this.pricingOfferRepository = pricingOfferRepository;
        this.applicationManagementPort = applicationManagementPort;
    }

    @Transactional
    public List<PricingOffer> execute(UUID applicationId) {
        ApplicationExpiryInfo expiryInfo = applicationManagementPort.getApplicationExpiryInfo(applicationId);
        if (expiryInfo.isExpired()) {
            applicationManagementPort.updateApplicationStatus(applicationId, "EXPIRED");
            throw new ApplicationExpiredException(applicationId);
        }

        return pricingOfferRepository.findByApplicationId(applicationId).stream()
                .filter(o -> PricingOfferStatus.ACTIVE.equals(o.getOfferStatus()))
                .filter(o -> !o.isExpired())
                .toList();
    }
}
