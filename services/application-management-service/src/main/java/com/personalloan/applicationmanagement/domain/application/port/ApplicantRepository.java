package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.Applicant;

import java.util.Optional;
import java.util.UUID;

public interface ApplicantRepository {
    Applicant save(Applicant applicant);
    Optional<Applicant> findByApplicationId(UUID applicationId);
}
