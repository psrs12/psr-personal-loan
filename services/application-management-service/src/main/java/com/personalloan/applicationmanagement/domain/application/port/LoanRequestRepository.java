package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.LoanRequest;

import java.util.Optional;
import java.util.UUID;

public interface LoanRequestRepository {
    LoanRequest save(LoanRequest loanRequest);
    Optional<LoanRequest> findByApplicationId(UUID applicationId);
}
