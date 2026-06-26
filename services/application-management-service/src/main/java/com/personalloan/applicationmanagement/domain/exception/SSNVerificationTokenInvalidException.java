package com.personalloan.applicationmanagement.domain.exception;

public class SSNVerificationTokenInvalidException extends RuntimeException {
    public SSNVerificationTokenInvalidException() {
        super("SSN verification token is missing or expired");
    }
}
