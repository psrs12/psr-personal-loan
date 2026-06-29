package com.personalloan.documentservice.application.document;

import com.personalloan.documentservice.domain.document.*;
import com.personalloan.documentservice.domain.document.port.*;
import com.personalloan.documentservice.domain.exception.DocumentCountSatisfiedException;
import com.personalloan.documentservice.domain.exception.DocumentTypeNotRequiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class UploadDocumentUseCase {

    private final DocumentRequirementRepository requirementRepository;
    private final DocumentRecordRepository recordRepository;
    private final StoragePort storagePort;
    private final DocumentEventPublisher eventPublisher;

    public UploadDocumentUseCase(DocumentRequirementRepository requirementRepository,
                                  DocumentRecordRepository recordRepository,
                                  StoragePort storagePort,
                                  DocumentEventPublisher eventPublisher) {
        this.requirementRepository = requirementRepository;
        this.recordRepository = recordRepository;
        this.storagePort = storagePort;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UUID execute(UUID applicationId, DocumentType documentType, MultipartFile file) {
        DocumentRequirement requirement = requirementRepository
                .findByApplicationIdAndDocumentType(applicationId, documentType)
                .orElseThrow(() -> new DocumentTypeNotRequiredException(documentType.name()));

        List<DocumentRecord> existing = recordRepository
                .findByApplicationIdAndDocumentType(applicationId, documentType).stream()
                .filter(r -> r.getStatus() != DocumentRecordStatus.REJECTED)
                .toList();

        if (existing.size() >= requirement.getCount()) {
            throw new DocumentCountSatisfiedException(documentType.name());
        }

        String storageRef = storagePort.store(applicationId, documentType.name(), file);
        DocumentRecord record = DocumentRecord.create(applicationId, requirement.getRequirementId(),
                documentType, storageRef);
        recordRepository.save(record);

        requirement.markUploaded();
        requirementRepository.save(requirement);

        eventPublisher.publishDocumentUploaded(applicationId, record.getDocumentId(), documentType.name());

        return record.getDocumentId();
    }
}
