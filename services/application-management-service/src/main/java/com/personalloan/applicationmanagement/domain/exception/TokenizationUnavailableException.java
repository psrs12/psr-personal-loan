package com.personalloan.applicationmanagement.domain.exception;

public class TokenizationUnavailableException extends RuntimeException {
    public TokenizationUnavailableException() {
        super("SSN tokenization service is unavailable");
    }
}
