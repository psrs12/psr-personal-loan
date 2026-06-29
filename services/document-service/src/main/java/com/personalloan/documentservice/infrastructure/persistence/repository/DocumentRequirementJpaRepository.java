package com.personalloan.documentservice.infrastructure.persistence.repository;

import com.personalloan.documentservice.infrastructure.persistence.entity.DocumentRequirementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRequirementJpaRepository extends JpaRepository<DocumentRequirementJpaEntity, UUID> {
    List<DocumentRequirementJpaEntity> findByApplicationId(UUID applicationId);
    Optional<DocumentRequirementJpaEntity> findByApplicationIdAndDocumentType(UUID applicationId, String documentType);
}
