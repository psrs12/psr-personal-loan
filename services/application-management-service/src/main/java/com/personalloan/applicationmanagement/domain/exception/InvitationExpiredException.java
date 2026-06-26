package com.personalloan.applicationmanagement.domain.exception;

public class InvitationExpiredException extends RuntimeException {
    public InvitationExpiredException(String invitationId) {
        super("Invitation has expired: " + invitationId);
    }
}
