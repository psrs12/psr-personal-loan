package com.personalloan.applicationmanagement.domain.exception;

import java.util.UUID;

public class ApplicationNotAccessibleException extends RuntimeException {
    public ApplicationNotAccessibleException(UUID applicationId) {
        super("Application is not accessible for re-entry: " + applicationId);
    }
}
