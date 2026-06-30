package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ApplicationAuditJpaAdapter implements ApplicationAuditRepository {

    private final ApplicationAuditJpaRepository auditRepo;

    public ApplicationAuditJpaAdapter(ApplicationAuditJpaRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    @Override
    public void save(ApplicationAuditRecord record) {
        ApplicationAuditJpaEntity entity = new ApplicationAuditJpaEntity();
        entity.setAuditId(record.auditId());
        entity.setApplicationId(record.applicationId());
        entity.setIntakeId(record.intakeId());
        entity.setEventType(record.eventType());
        entity.setEventTimestamp(record.eventTimestamp());
        entity.setPayload(record.payload());
        auditRepo.save(entity);
    }

    @Override
    public List<ApplicationAuditRecord> findByApplicationId(UUID applicationId) {
        return auditRepo.findByApplicationIdOrderByEventTimestampAsc(applicationId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ApplicationAuditRecord> findByApplicationIdOrIntakeId(UUID applicationId, UUID intakeId) {
        return auditRepo.findByApplicationIdOrIntakeIdOrderByEventTimestampAsc(applicationId, intakeId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ApplicationAuditRecord toDomain(ApplicationAuditJpaEntity e) {
        return new ApplicationAuditRecord(e.getAuditId(), e.getApplicationId(), e.getIntakeId(),
                e.getEventType(), e.getEventTimestamp(), e.getPayload());
    }
}
