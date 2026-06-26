package com.personalloan.applicationmanagement.domain.application;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;

import java.time.LocalDateTime;
import java.util.UUID;

public class Application {

    private final UUID applicationId;
    private final UUID intakeId;
    private final ApplicationSource applicationSource;
    private ApplicationStatus applicationStatus;
    private final LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    private Application(UUID applicationId, UUID intakeId, ApplicationSource applicationSource,
                         ApplicationStatus applicationStatus, LocalDateTime createdTimestamp) {
        this.applicationId = applicationId;
        this.intakeId = intakeId;
        this.applicationSource = applicationSource;
        this.applicationStatus = applicationStatus;
        this.createdTimestamp = createdTimestamp;
    }

    public static Application create(UUID intakeId, ApplicationSource source) {
        return new Application(UUID.randomUUID(), intakeId, source, ApplicationStatus.CREATED, LocalDateTime.now());
    }

    public static Application createDirect() {
        return new Application(UUID.randomUUID(), null, ApplicationSource.DIRECT, ApplicationStatus.CREATED, LocalDateTime.now());
    }

    public static Application reconstitute(UUID applicationId, UUID intakeId, ApplicationSource applicationSource,
                                            ApplicationStatus applicationStatus, LocalDateTime createdTimestamp,
                                            LocalDateTime updatedTimestamp) {
        Application app = new Application(applicationId, intakeId, applicationSource, applicationStatus, createdTimestamp);
        app.updatedTimestamp = updatedTimestamp;
        return app;
    }

    public boolean isActive() {
        return applicationStatus == ApplicationStatus.CREATED
                || applicationStatus == ApplicationStatus.IN_PROGRESS
                || applicationStatus == ApplicationStatus.READY_FOR_SUBMISSION
                || applicationStatus == ApplicationStatus.SUBMITTED
                || applicationStatus == ApplicationStatus.PROCESSING;
    }

    public UUID getApplicationId() { return applicationId; }
    public UUID getIntakeId() { return intakeId; }
    public ApplicationSource getApplicationSource() { return applicationSource; }
    public ApplicationStatus getApplicationStatus() { return applicationStatus; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
}
