package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.Applicant;

public interface ApplicantRepository {
    Applicant save(Applicant applicant);
}
