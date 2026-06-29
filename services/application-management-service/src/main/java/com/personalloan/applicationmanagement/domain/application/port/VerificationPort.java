package com.personalloan.applicationmanagement.domain.application.port;

import java.time.LocalDate;
import java.util.UUID;

public interface VerificationPort {
    boolean verify(UUID applicationId, String last4SSN, LocalDate dateOfBirth);
}
