package com.personalloan.documentservice.domain.exception;

public class DocumentCountSatisfiedException extends RuntimeException {
    public DocumentCountSatisfiedException(String documentType) {
        super("Required document count already satisfied for type: " + documentType);
    }
}
