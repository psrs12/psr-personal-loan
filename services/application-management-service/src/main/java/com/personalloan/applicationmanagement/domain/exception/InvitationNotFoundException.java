package com.personalloan.applicationmanagement.domain.exception;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException(String invitationId) {
        super("Invitation not found: " + invitationId);
    }
}
