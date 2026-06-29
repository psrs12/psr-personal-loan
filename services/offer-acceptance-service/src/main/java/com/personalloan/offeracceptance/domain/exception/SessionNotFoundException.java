package com.personalloan.offeracceptance.domain.exception;

import java.util.UUID;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(UUID applicationId) {
        super("No offer acceptance session found for application: " + applicationId);
    }
}
