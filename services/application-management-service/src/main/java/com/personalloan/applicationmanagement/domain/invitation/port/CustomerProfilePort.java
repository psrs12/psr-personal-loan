package com.personalloan.applicationmanagement.domain.invitation.port;

import com.personalloan.applicationmanagement.domain.invitation.CustomerPrefill;

import java.util.Optional;

public interface CustomerProfilePort {
    Optional<CustomerPrefill> retrieveCustomer(String customerReferenceId);
}
