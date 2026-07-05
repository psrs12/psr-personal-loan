package com.personalloan.pricingorchestration.infrastructure.persistence.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRecordJpaRepository extends JpaRepository<ConsentRecordJpaEntity, UUID> {
    List<ConsentRecordJpaEntity> findByApplicationId(UUID applicationId);
    Optional<ConsentRecordJpaEntity> findByApplicationIdAndConsentType(UUID applicationId, String consentType);
}
