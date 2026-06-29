package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.LoanRequest;

public interface LoanRequestRepository {
    LoanRequest save(LoanRequest loanRequest);
}
