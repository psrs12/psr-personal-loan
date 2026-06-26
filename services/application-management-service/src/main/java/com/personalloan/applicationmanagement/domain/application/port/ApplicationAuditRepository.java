package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;

public interface ApplicationAuditRepository {
    void save(ApplicationAuditRecord record);
}
