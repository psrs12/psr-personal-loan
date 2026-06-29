package com.personalloan.applicationmanagement.domain.pricing.port;

import com.personalloan.applicationmanagement.domain.pricing.OfferSelection;

import java.util.Optional;
import java.util.UUID;

public interface OfferSelectionRepository {
    OfferSelection save(OfferSelection selection);
    Optional<OfferSelection> findByApplicationId(UUID applicationId);
}
