package com.personalloan.documentservice.domain.exception;

public class DocumentTypeNotRequiredException extends RuntimeException {
    public DocumentTypeNotRequiredException(String documentType) {
        super("Document type not required for this application: " + documentType);
    }
}
