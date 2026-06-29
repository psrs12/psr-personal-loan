package com.personalloan.documentservice.domain.document.port;

import java.util.UUID;

public interface DocumentEventPublisher {
    void publishDocumentUploaded(UUID applicationId, UUID documentId, String documentType);
    void publishDocumentRejected(UUID applicationId, UUID documentId, String reason);
    void publishDocumentsCompleted(UUID applicationId);
}
