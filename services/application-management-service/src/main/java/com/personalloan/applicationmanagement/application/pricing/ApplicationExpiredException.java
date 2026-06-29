package com.personalloan.applicationmanagement.application.pricing;

import java.util.UUID;

public class ApplicationExpiredException extends RuntimeException {
    public ApplicationExpiredException(UUID applicationId) {
        super("Application " + applicationId + " has expired");
    }
}
