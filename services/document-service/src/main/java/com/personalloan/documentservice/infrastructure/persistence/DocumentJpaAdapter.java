package com.personalloan.documentservice.infrastructure.persistence;

import com.personalloan.documentservice.domain.document.*;
import com.personalloan.documentservice.domain.document.port.DocumentRequirementRepository;
import com.personalloan.documentservice.infrastructure.persistence.entity.DocumentRequirementJpaEntity;
import com.personalloan.documentservice.infrastructure.persistence.repository.DocumentRequirementJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DocumentJpaAdapter implements DocumentRequirementRepository {

    private final DocumentRequirementJpaRepository requirementJpaRepo;

    public DocumentJpaAdapter(DocumentRequirementJpaRepository requirementJpaRepo) {
        this.requirementJpaRepo = requirementJpaRepo;
    }

    @Override
    public DocumentRequirement save(DocumentRequirement requirement) {
        requirementJpaRepo.save(toRequirementEntity(requirement));
        return requirement;
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
}
