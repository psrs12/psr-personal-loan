package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationExpiryConfigJpaRepository extends JpaRepository<ApplicationExpiryConfigJpaEntity, UUID> {
    Optional<ApplicationExpiryConfigJpaEntity> findByProductTypeAndChannel(String productType, String channel);
}
