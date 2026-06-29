package com.personalloan.offeracceptance.domain.port;

import com.personalloan.offeracceptance.domain.offer.OfferAcceptanceSession;

import java.util.Optional;
import java.util.UUID;

public interface OfferAcceptanceSessionRepository {
    void save(OfferAcceptanceSession session);
    Optional<OfferAcceptanceSession> findByApplicationId(UUID applicationId);
    boolean existsByApplicationId(UUID applicationId);
}
