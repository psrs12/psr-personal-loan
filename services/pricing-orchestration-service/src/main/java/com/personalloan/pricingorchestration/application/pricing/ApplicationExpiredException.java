package com.personalloan.pricingorchestration.application.pricing;

import java.util.UUID;

public class ApplicationExpiredException extends RuntimeException {

    public ApplicationExpiredException(UUID applicationId) {
        super("Application has expired: " + applicationId);
    }
}
