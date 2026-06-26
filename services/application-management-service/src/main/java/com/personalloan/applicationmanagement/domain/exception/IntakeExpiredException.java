package com.personalloan.applicationmanagement.domain.exception;

public class IntakeExpiredException extends RuntimeException {
    public IntakeExpiredException(String intakeId) {
        super("Intake session has expired: " + intakeId);
    }
}
