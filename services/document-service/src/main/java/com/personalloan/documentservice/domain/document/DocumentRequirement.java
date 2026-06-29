package com.personalloan.documentservice.domain.document;

import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentRequirement {

    private final UUID requirementId;
    private final UUID applicationId;
    private final DocumentType documentType;
    private final int count;
    private final String description;
    private DocumentRequirementStatus status;
    private final LocalDateTime createdAt;

    private DocumentRequirement(UUID requirementId, UUID applicationId, DocumentType documentType,
                                  int count, String description, DocumentRequirementStatus status,
                                  LocalDateTime createdAt) {
        this.requirementId = requirementId;
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.count = count;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static DocumentRequirement create(UUID applicationId, DocumentType documentType,
                                              int count, String description) {
        return new DocumentRequirement(UUID.randomUUID(), applicationId, documentType,
                count, description, DocumentRequirementStatus.PENDING, LocalDateTime.now());
    }

    public static DocumentRequirement reconstitute(UUID requirementId, UUID applicationId,
                                                    DocumentType documentType, int count, String description,
                                                    DocumentRequirementStatus status, LocalDateTime createdAt) {
        return new DocumentRequirement(requirementId, applicationId, documentType,
                count, description, status, createdAt);
    }

    public void markUploaded() { this.status = DocumentRequirementStatus.UPLOADED; }
    public void markRejected() { this.status = DocumentRequirementStatus.REJECTED; }
    public void markCompleted() { this.status = DocumentRequirementStatus.COMPLETED; }

    public UUID getRequirementId() { return requirementId; }
    public UUID getApplicationId() { return applicationId; }
    public DocumentType getDocumentType() { return documentType; }
    public int getCount() { return count; }
    public String getDescription() { return description; }
    public DocumentRequirementStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
