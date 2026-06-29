package com.personalloan.documentservice.domain.document.port;

import java.util.UUID;

public interface ApplicationManagementPort {
    void updateApplicationStatus(UUID applicationId, String status);
}
