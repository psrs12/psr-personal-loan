package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationJpaEntity, UUID> {

    @Query("""
            SELECT COUNT(a) > 0 FROM ApplicationJpaEntity a
            JOIN ApplicationIntakeContextJpaEntity c ON a.intakeId = c.intakeId
            WHERE c.invitationId = :invitationId
            AND a.applicationStatus IN ('CREATED','IN_PROGRESS','READY_FOR_SUBMISSION','SUBMITTED','PROCESSING')
            """)
    boolean existsActiveApplicationForInvitation(@Param("invitationId") String invitationId);
}
