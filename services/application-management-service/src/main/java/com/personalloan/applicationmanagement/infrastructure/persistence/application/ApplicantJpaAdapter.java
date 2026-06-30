package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.*;
import com.personalloan.applicationmanagement.domain.application.port.ApplicantRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ApplicantJpaAdapter implements ApplicantRepository {

    private final ApplicantJpaRepository applicantRepo;

    public ApplicantJpaAdapter(ApplicantJpaRepository applicantRepo) {
        this.applicantRepo = applicantRepo;
    }

    @Override
    public Applicant save(Applicant applicant) {
        applicantRepo.save(toEntity(applicant));
        return applicant;
    }

    @Override
    public Optional<Applicant> findByApplicationId(UUID applicationId) {
        return applicantRepo.findByApplicationId(applicationId).map(this::toDomain);
    }

    private Applicant toDomain(ApplicantJpaEntity e) {
        return Applicant.reconstitute(
                e.getApplicantId(), e.getApplicationId(), e.getFirstName(), e.getLastName(),
                e.getDateOfBirth(), Citizenship.valueOf(e.getCitizenship()), e.getSsnToken(),
                e.getEmail(), e.getPhone(), e.getStreet(), e.getCity(), e.getState(), e.getZip(),
                e.getEmployerName(),
                e.getEmploymentStatus() != null ? EmploymentStatus.valueOf(e.getEmploymentStatus()) : null,
                e.getAnnualIncome(), e.getCreatedTimestamp());
    }

    private ApplicantJpaEntity toEntity(Applicant a) {
        ApplicantJpaEntity e = new ApplicantJpaEntity();
        e.setApplicantId(a.getApplicantId());
        e.setApplicationId(a.getApplicationId());
        e.setFirstName(a.getFirstName());
        e.setLastName(a.getLastName());
        e.setDateOfBirth(a.getDateOfBirth());
        e.setCitizenship(a.getCitizenship().name());
        e.setSsnToken(a.getSsnToken());
        e.setEmail(a.getEmail());
        e.setPhone(a.getPhone());
        e.setStreet(a.getStreet());
        e.setCity(a.getCity());
        e.setState(a.getState());
        e.setZip(a.getZip());
        e.setEmployerName(a.getEmployerName());
        e.setEmploymentStatus(a.getEmploymentStatus() != null ? a.getEmploymentStatus().name() : null);
        e.setAnnualIncome(a.getAnnualIncome());
        e.setCreatedTimestamp(a.getCreatedTimestamp());
        return e;
    }
}
