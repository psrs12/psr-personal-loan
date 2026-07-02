package com.personalloan.pricingorchestration.infrastructure.persistence.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OfferSelectionJpaRepository extends JpaRepository<OfferSelectionJpaEntity, UUID> {
    Optional<OfferSelectionJpaEntity> findByApplicationId(UUID applicationId);
}
