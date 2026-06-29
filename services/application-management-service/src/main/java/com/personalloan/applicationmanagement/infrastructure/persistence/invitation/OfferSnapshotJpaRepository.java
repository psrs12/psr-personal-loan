package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OfferSnapshotJpaRepository extends JpaRepository<OfferSnapshotJpaEntity, UUID> {
    Optional<OfferSnapshotJpaEntity> findBySessionId(UUID sessionId);
}
