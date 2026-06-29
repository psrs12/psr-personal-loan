package com.personalloan.documentservice.infrastructure.persistence.repository;

import com.personalloan.documentservice.infrastructure.persistence.entity.DocumentRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRecordJpaRepository extends JpaRepository<DocumentRecordJpaEntity, UUID> {
    Optional<DocumentRecordJpaEntity> findByDocumentId(UUID documentId);
    List<DocumentRecordJpaEntity> findByApplicationIdAndDocumentType(UUID applicationId, String documentType);
}
