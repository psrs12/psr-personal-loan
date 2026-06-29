package com.personalloan.offeracceptance.infrastructure.persistence.repository;

import com.personalloan.offeracceptance.infrastructure.persistence.entity.OfferAcceptanceSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OfferAcceptanceSessionJpaRepository extends JpaRepository<OfferAcceptanceSessionJpaEntity, UUID> {
    Optional<OfferAcceptanceSessionJpaEntity> findByApplicationId(UUID applicationId);
    boolean existsByApplicationId(UUID applicationId);
}
