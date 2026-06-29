package com.personalloan.applicationmanagement.infrastructure.verification;

import com.personalloan.applicationmanagement.domain.application.Applicant;
import com.personalloan.applicationmanagement.domain.application.port.ApplicantRepository;
import com.personalloan.applicationmanagement.domain.application.port.VerificationPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "verification.stub.enabled", havingValue = "true", matchIfMissing = true)
public class StubVerificationAdapter implements VerificationPort {

    private final ApplicantRepository applicantRepository;

    public StubVerificationAdapter(ApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    @Override
    public boolean verify(UUID applicationId, String last4SSN, LocalDate dateOfBirth) {
        return applicantRepository.findByApplicationId(applicationId)
                .map(applicant -> matchesLast4(applicant, last4SSN) && matchesDob(applicant, dateOfBirth))
                .orElse(false);
    }

    private boolean matchesLast4(Applicant applicant, String last4SSN) {
        String token = applicant.getSsnToken();
        if (token == null || token.length() < 4) return false;
        return token.substring(token.length() - 4).equals(last4SSN);
    }

    private boolean matchesDob(Applicant applicant, LocalDate dateOfBirth) {
        return dateOfBirth != null && dateOfBirth.equals(applicant.getDateOfBirth());
    }
}
