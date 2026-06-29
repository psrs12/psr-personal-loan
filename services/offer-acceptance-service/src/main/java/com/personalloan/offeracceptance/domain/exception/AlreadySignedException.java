package com.personalloan.offeracceptance.domain.exception;

import java.util.UUID;

public class AlreadySignedException extends RuntimeException {
    public AlreadySignedException(UUID applicationId) {
        super("Offer already signed for application: " + applicationId);
    }
}
