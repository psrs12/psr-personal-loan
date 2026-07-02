package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.LoanRequest;
import com.personalloan.applicationmanagement.domain.application.port.LoanRequestRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LoanRequestJpaAdapter implements LoanRequestRepository {

    private final LoanRequestJpaRepository loanRequestRepo;

    public LoanRequestJpaAdapter(LoanRequestJpaRepository loanRequestRepo) {
        this.loanRequestRepo = loanRequestRepo;
    }

    @Override
    public LoanRequest save(LoanRequest loanRequest) {
        loanRequestRepo.save(toEntity(loanRequest));
        return loanRequest;
    }

    @Override
    public Optional<LoanRequest> findByApplicationId(UUID applicationId) {
        return loanRequestRepo.findByApplicationId(applicationId).map(this::toDomain);
    }

    private LoanRequestJpaEntity toEntity(LoanRequest lr) {
        LoanRequestJpaEntity e = new LoanRequestJpaEntity();
        e.setLoanRequestId(lr.getLoanRequestId());
        e.setApplicationId(lr.getApplicationId());
        e.setRequestedAmount(lr.getRequestedAmount());
        e.setTermMonths(lr.getTermMonths());
        e.setLoanPurpose(lr.getLoanPurpose());
        e.setCreatedTimestamp(lr.getCreatedTimestamp());
        return e;
    }

    private LoanRequest toDomain(LoanRequestJpaEntity e) {
        return LoanRequest.reconstitute(e.getLoanRequestId(), e.getApplicationId(), e.getRequestedAmount(),
                e.getTermMonths(), e.getLoanPurpose(), e.getCreatedTimestamp());
    }
}
