package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application_audit")
public class ApplicationAuditJpaEntity {

    @Id
    @Column(name = "audit_id")
    private UUID auditId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "intake_id")
    private UUID intakeId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    public UUID getAuditId() { return auditId; }
    public void setAuditId(UUID auditId) { this.auditId = auditId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getIntakeId() { return intakeId; }
    public void setIntakeId(UUID intakeId) { this.intakeId = intakeId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
