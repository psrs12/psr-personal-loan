package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;

import java.util.List;
import java.util.UUID;

public interface ApplicationAuditRepository {
    void save(ApplicationAuditRecord record);
    List<ApplicationAuditRecord> findByApplicationId(UUID applicationId);
    List<ApplicationAuditRecord> findByApplicationIdOrIntakeId(UUID applicationId, UUID intakeId);
}
