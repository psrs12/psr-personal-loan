package com.personalloan.documentservice.domain.document.port;

import com.personalloan.documentservice.domain.document.DocumentRequirement;
import com.personalloan.documentservice.domain.document.DocumentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRequirementRepository {
    DocumentRequirement save(DocumentRequirement requirement);
    List<DocumentRequirement> findByApplicationId(UUID applicationId);
    Optional<DocumentRequirement> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType);
}
