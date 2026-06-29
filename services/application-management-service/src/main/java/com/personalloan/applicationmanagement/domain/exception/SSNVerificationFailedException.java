package com.personalloan.applicationmanagement.domain.exception;

public class SSNVerificationFailedException extends RuntimeException {
    public SSNVerificationFailedException() {
        super("SSN could not be verified");
    }
}
