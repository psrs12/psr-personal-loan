package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.LoanRequest;
import com.personalloan.applicationmanagement.domain.application.port.LoanRequestRepository;
import org.springframework.stereotype.Component;

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
}
