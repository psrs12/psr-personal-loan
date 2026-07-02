package com.personalloan.pricingorchestration.domain.pricing.port;

import com.personalloan.pricingorchestration.domain.pricing.OfferSelection;

import java.util.Optional;
import java.util.UUID;

public interface OfferSelectionRepository {
    OfferSelection save(OfferSelection selection);
    Optional<OfferSelection> findByApplicationId(UUID applicationId);
}
