package com.personalloan.documentservice.infrastructure.persistence;

import com.personalloan.documentservice.domain.document.*;
import com.personalloan.documentservice.domain.document.port.DocumentRecordRepository;
import com.personalloan.documentservice.infrastructure.persistence.entity.DocumentRecordJpaEntity;
import com.personalloan.documentservice.infrastructure.persistence.repository.DocumentRecordJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DocumentRecordJpaAdapter implements DocumentRecordRepository {

    private final DocumentRecordJpaRepository recordJpaRepo;

    public DocumentRecordJpaAdapter(DocumentRecordJpaRepository recordJpaRepo) {
        this.recordJpaRepo = recordJpaRepo;
    }

    @Override
    public DocumentRecord save(DocumentRecord record) {
        recordJpaRepo.save(toRecordEntity(record));
        return record;
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
