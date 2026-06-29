package com.personalloan.applicationmanagement.domain.exception;

import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(ApplicationStatus from, ApplicationStatus to) {
        super("Invalid state transition from " + from + " to " + to);
    }
}
