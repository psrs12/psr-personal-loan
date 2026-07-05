package com.personalloan.pricingorchestration.infrastructure.persistence.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PricingOfferJpaRepository extends JpaRepository<PricingOfferJpaEntity, UUID> {
    List<PricingOfferJpaEntity> findByApplicationId(UUID applicationId);
}
