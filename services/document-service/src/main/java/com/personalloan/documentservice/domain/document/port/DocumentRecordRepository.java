package com.personalloan.documentservice.domain.document.port;

import com.personalloan.documentservice.domain.document.DocumentRecord;
import com.personalloan.documentservice.domain.document.DocumentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRecordRepository {
    DocumentRecord save(DocumentRecord record);
    List<DocumentRecord> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType);
    Optional<DocumentRecord> findByDocumentId(UUID documentId);
}
