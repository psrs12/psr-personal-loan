package com.personalloan.documentservice.domain.document;

import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentRecord {

    private final UUID documentId;
    private final UUID applicationId;
    private final UUID requirementId;
    private final DocumentType documentType;
    private final String storageReference;
    private DocumentRecordStatus status;
    private final LocalDateTime uploadedAt;

    private DocumentRecord(UUID documentId, UUID applicationId, UUID requirementId,
                            DocumentType documentType, String storageReference,
                            DocumentRecordStatus status, LocalDateTime uploadedAt) {
        this.documentId = documentId;
        this.applicationId = applicationId;
        this.requirementId = requirementId;
        this.documentType = documentType;
        this.storageReference = storageReference;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public static DocumentRecord create(UUID applicationId, UUID requirementId, DocumentType documentType,
                                         String storageReference) {
        return new DocumentRecord(UUID.randomUUID(), applicationId, requirementId,
                documentType, storageReference, DocumentRecordStatus.UPLOADED, LocalDateTime.now());
    }

    public static DocumentRecord reconstitute(UUID documentId, UUID applicationId, UUID requirementId,
                                               DocumentType documentType, String storageReference,
                                               DocumentRecordStatus status, LocalDateTime uploadedAt) {
        return new DocumentRecord(documentId, applicationId, requirementId, documentType,
                storageReference, status, uploadedAt);
    }

    public void markVerified() { this.status = DocumentRecordStatus.VERIFIED; }
    public void markRejected() { this.status = DocumentRecordStatus.REJECTED; }
    public void markScanning() { this.status = DocumentRecordStatus.SCANNING; }

    public UUID getDocumentId() { return documentId; }
    public UUID getApplicationId() { return applicationId; }
    public UUID getRequirementId() { return requirementId; }
    public DocumentType getDocumentType() { return documentType; }
    public String getStorageReference() { return storageReference; }
    public DocumentRecordStatus getStatus() { return status; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
