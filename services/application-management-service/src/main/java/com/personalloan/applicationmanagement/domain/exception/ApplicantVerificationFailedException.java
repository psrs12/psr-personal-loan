package com.personalloan.applicationmanagement.domain.exception;

public class ApplicantVerificationFailedException extends RuntimeException {
    public ApplicantVerificationFailedException() {
        super("Applicant verification failed");
    }
}
