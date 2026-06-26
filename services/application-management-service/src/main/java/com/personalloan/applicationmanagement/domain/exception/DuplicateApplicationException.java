package com.personalloan.applicationmanagement.domain.exception;

public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException(String invitationId) {
        super("Active application already exists for invitation: " + invitationId);
    }
}
