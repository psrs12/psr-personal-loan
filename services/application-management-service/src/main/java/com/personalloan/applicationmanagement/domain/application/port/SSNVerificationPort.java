package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.SSNVerificationToken;

public interface SSNVerificationPort {
    SSNVerificationToken verifySSN(String ssn);
}
