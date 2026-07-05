package com.personalloan.applicationmanagement.domain.application;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

    @Test
    void create_withIntakeId_setsSourceAndStatus() {
        UUID intakeId = UUID.randomUUID();

        Application application = Application.create(intakeId, ApplicationSource.INVITATION);

        assertThat(application.getApplicationId()).isNotNull();
        assertThat(application.getIntakeId()).isEqualTo(intakeId);
        assertThat(application.getApplicationSource()).isEqualTo(ApplicationSource.INVITATION);
        assertThat(application.getApplicationStatus()).isEqualTo(ApplicationStatus.CREATED);
        assertThat(application.getCreatedTimestamp()).isNotNull();
    }

    @Test
    void createDirect_setsNullIntakeIdAndDirectSource() {
        Application application = Application.createDirect();

        assertThat(application.getIntakeId()).isNull();
        assertThat(application.getApplicationSource()).isEqualTo(ApplicationSource.DIRECT);
        assertThat(application.getApplicationStatus()).isEqualTo(ApplicationStatus.CREATED);
    }

    @Test
    void isActive_returnsTrue_forCreatedStatus() {
        Application application = Application.createDirect();

        assertThat(application.isActive()).isTrue();
    }

    @Test
    void isActive_returnsFalse_forApprovedStatus() {
        Application application = Application.reconstitute(
                UUID.randomUUID(), null, ApplicationSource.DIRECT,
                ApplicationStatus.APPROVED, java.time.LocalDateTime.now(), java.time.LocalDateTime.now(),
                null, null, null, null, null
        );

        assertThat(application.isActive()).isFalse();
    }

    @Test
    void isActive_returnsFalse_forDeclinedStatus() {
        Application application = Application.reconstitute(
                UUID.randomUUID(), null, ApplicationSource.DIRECT,
                ApplicationStatus.DECLINED, java.time.LocalDateTime.now(), java.time.LocalDateTime.now(),
                null, null, null, null, null
        );

        assertThat(application.isActive()).isFalse();
    }
}
