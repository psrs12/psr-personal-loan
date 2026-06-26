package com.personalloan.applicationmanagement.application.application;

import com.personalloan.applicationmanagement.domain.application.SSNVerificationToken;
import com.personalloan.applicationmanagement.domain.application.port.BoltTokenizationPort;
import com.personalloan.applicationmanagement.domain.application.port.SSNVerificationPort;
import org.springframework.stereotype.Service;

@Service
public class VerifySSNUseCase {

    private final BoltTokenizationPort boltTokenizationPort;
    private final SSNVerificationPort ssnVerificationPort;
    private final SSNTokenStore ssnTokenStore;

    public VerifySSNUseCase(BoltTokenizationPort boltTokenizationPort,
                             SSNVerificationPort ssnVerificationPort,
                             SSNTokenStore ssnTokenStore) {
        this.boltTokenizationPort = boltTokenizationPort;
        this.ssnVerificationPort = ssnVerificationPort;
        this.ssnTokenStore = ssnTokenStore;
    }

    public SSNVerificationToken execute(String rawSsn) {
        String ssnToken = boltTokenizationPort.tokenize(rawSsn);
        SSNVerificationToken verificationToken = ssnVerificationPort.verifySSN(ssnToken);
        ssnTokenStore.store(verificationToken.token(), verificationToken.expiresAt());
        return verificationToken;
    }
}
