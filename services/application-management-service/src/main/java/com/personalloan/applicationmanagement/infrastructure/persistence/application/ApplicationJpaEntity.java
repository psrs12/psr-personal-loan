package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application")
public class ApplicationJpaEntity {

    @Id
    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "intake_id")
    private UUID intakeId;

    @Column(name = "application_source", nullable = false)
    private String applicationSource;

    @Column(name = "application_status", nullable = false)
    private String applicationStatus;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimestamp;

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getIntakeId() { return intakeId; }
    public void setIntakeId(UUID intakeId) { this.intakeId = intakeId; }
    public String getApplicationSource() { return applicationSource; }
    public void setApplicationSource(String applicationSource) { this.applicationSource = applicationSource; }
    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String applicationStatus) { this.applicationStatus = applicationStatus; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }
    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }
}
