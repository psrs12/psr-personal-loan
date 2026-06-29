package com.personalloan.documentservice.infrastructure.persistence;

import com.personalloan.documentservice.domain.document.*;
import com.personalloan.documentservice.domain.document.port.DocumentRecordRepository;
import com.personalloan.documentservice.domain.document.port.DocumentRequirementRepository;
import com.personalloan.documentservice.infrastructure.persistence.entity.DocumentRecordJpaEntity;
import com.personalloan.documentservice.infrastructure.persistence.entity.DocumentRequirementJpaEntity;
import com.personalloan.documentservice.infrastructure.persistence.repository.DocumentRecordJpaRepository;
import com.personalloan.documentservice.infrastructure.persistence.repository.DocumentRequirementJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DocumentJpaAdapter implements DocumentRequirementRepository, DocumentRecordRepository {

    private final DocumentRequirementJpaRepository requirementJpaRepo;
    private final DocumentRecordJpaRepository recordJpaRepo;

    public DocumentJpaAdapter(DocumentRequirementJpaRepository requirementJpaRepo,
                               DocumentRecordJpaRepository recordJpaRepo) {
        this.requirementJpaRepo = requirementJpaRepo;
        this.recordJpaRepo = recordJpaRepo;
    }

    // --- DocumentRequirementRepository ---

    @Override
    public void save(DocumentRequirement requirement) {
        requirementJpaRepo.save(toRequirementEntity(requirement));
    }

    @Override
    public Optional<DocumentRequirement> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType) {
        return requirementJpaRepo.findByApplicationIdAndDocumentType(applicationId, documentType.name())
                .map(this::toRequirementDomain);
    }

    @Override
    public List<DocumentRequirement> findByApplicationId(UUID applicationId) {
        return requirementJpaRepo.findByApplicationId(applicationId).stream()
                .map(this::toRequirementDomain)
                .toList();
    }

    // --- DocumentRecordRepository ---

    @Override
    public void save(DocumentRecord record) {
        recordJpaRepo.save(toRecordEntity(record));
    }

    @Override
    public Optional<DocumentRecord> findByDocumentId(UUID documentId) {
        return recordJpaRepo.findByDocumentId(documentId).map(this::toRecordDomain);
    }

    @Override
    public List<DocumentRecord> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType) {
        return recordJpaRepo.findByApplicationIdAndDocumentType(applicationId, documentType.name()).stream()
                .map(this::toRecordDomain)
                .toList();
    }

    // --- Mapping ---

    private DocumentRequirementJpaEntity toRequirementEntity(DocumentRequirement req) {
        DocumentRequirementJpaEntity e = new DocumentRequirementJpaEntity();
        e.setRequirementId(req.getRequirementId());
        e.setApplicationId(req.getApplicationId());
        e.setDocumentType(req.getDocumentType().name());
        e.setCount(req.getCount());
        e.setDescription(req.getDescription());
        e.setStatus(req.getStatus().name());
        e.setCreatedAt(req.getCreatedAt());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }

    private DocumentRequirement toRequirementDomain(DocumentRequirementJpaEntity e) {
        return DocumentRequirement.reconstitute(
                e.getRequirementId(),
                e.getApplicationId(),
                DocumentType.valueOf(e.getDocumentType()),
                e.getCount(),
                e.getDescription(),
                DocumentRequirementStatus.valueOf(e.getStatus()),
                e.getCreatedAt()
        );
    }

    private DocumentRecordJpaEntity toRecordEntity(DocumentRecord rec) {
        DocumentRecordJpaEntity e = new DocumentRecordJpaEntity();
        e.setDocumentId(rec.getDocumentId());
        e.setApplicationId(rec.getApplicationId());
        e.setRequirementId(rec.getRequirementId());
        e.setDocumentType(rec.getDocumentType().name());
        e.setStorageRef(rec.getStorageReference());
        e.setStatus(rec.getStatus().name());
        e.setCreatedAt(rec.getUploadedAt());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }

    private DocumentRecord toRecordDomain(DocumentRecordJpaEntity e) {
        return DocumentRecord.reconstitute(
                e.getDocumentId(),
                e.getApplicationId(),
                e.getRequirementId(),
                DocumentType.valueOf(e.getDocumentType()),
                e.getStorageRef(),
                DocumentRecordStatus.valueOf(e.getStatus()),
                e.getCreatedAt()
        );
    }
}
