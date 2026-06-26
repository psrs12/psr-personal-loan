package com.personalloan.applicationmanagement.domain.pricing.port;

import com.personalloan.applicationmanagement.domain.pricing.PricingOffer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingOfferRepository {
    PricingOffer save(PricingOffer offer);
    List<PricingOffer> findByApplicationId(UUID applicationId);
    Optional<PricingOffer> findById(UUID pricingOfferId);
    void saveAll(List<PricingOffer> offers);
}
