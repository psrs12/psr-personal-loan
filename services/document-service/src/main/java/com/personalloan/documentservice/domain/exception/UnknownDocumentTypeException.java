package com.personalloan.documentservice.domain.exception;

public class UnknownDocumentTypeException extends RuntimeException {
    public UnknownDocumentTypeException(String code) {
        super("Unknown Decision Engine document type code: " + code);
    }
}
