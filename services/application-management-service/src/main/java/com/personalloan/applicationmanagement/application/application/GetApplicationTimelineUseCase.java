package com.personalloan.applicationmanagement.application.application;

import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetApplicationTimelineUseCase {

    private final ApplicationRepository applicationRepository;
    private final ApplicationAuditRepository applicationAuditRepository;

    public GetApplicationTimelineUseCase(ApplicationRepository applicationRepository,
                                          ApplicationAuditRepository applicationAuditRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationAuditRepository = applicationAuditRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationAuditRecord> execute(UUID applicationId) {
        Application application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (application.getIntakeId() != null) {
            return applicationAuditRepository.findByApplicationIdOrIntakeId(applicationId, application.getIntakeId());
        }

        return applicationAuditRepository.findByApplicationId(applicationId);
    }
}
