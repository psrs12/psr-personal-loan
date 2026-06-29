package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApplicationAuditJpaRepository extends JpaRepository<ApplicationAuditJpaEntity, UUID> {

    List<ApplicationAuditJpaEntity> findByApplicationIdOrderByEventTimestampAsc(UUID applicationId);

    @Query("SELECT a FROM ApplicationAuditJpaEntity a WHERE a.applicationId = :applicationId OR a.intakeId = :intakeId ORDER BY a.eventTimestamp ASC")
    List<ApplicationAuditJpaEntity> findByApplicationIdOrIntakeIdOrderByEventTimestampAsc(
            @Param("applicationId") UUID applicationId,
            @Param("intakeId") UUID intakeId);
}
