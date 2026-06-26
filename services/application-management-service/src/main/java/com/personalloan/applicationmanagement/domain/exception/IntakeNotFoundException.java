package com.personalloan.applicationmanagement.domain.exception;

public class IntakeNotFoundException extends RuntimeException {
    public IntakeNotFoundException(String intakeId) {
        super("Intake context not found: " + intakeId);
    }
}
