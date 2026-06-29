package com.personalloan.pricingorchestration.domain.pricing.port;

import java.util.UUID;

public interface CreditManagementPort {
    String initiateSoftPull(UUID applicationId, String applicantReference);
    String initiateHardPull(UUID applicationId, String applicantReference, String softPullReferenceId);
}
