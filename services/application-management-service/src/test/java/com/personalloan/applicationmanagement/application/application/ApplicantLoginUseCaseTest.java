package com.personalloan.applicationmanagement.application.application;

import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.application.port.VerificationPort;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotAccessibleException;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import com.personalloan.applicationmanagement.domain.exception.ApplicantVerificationFailedException;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantLoginUseCaseTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private VerificationPort verificationPort;

    private ApplicantLoginUseCase useCase;

    private final UUID applicationId = UUID.randomUUID();
    private final String last4SSN = "1234";
    private final LocalDate dob = LocalDate.of(1990, 1, 15);
    private final String jwtSecret = "test-secret-key-must-be-at-least-32-chars-long";

    @BeforeEach
    void setUp() {
        useCase = new ApplicantLoginUseCase(applicationRepository, verificationPort, jwtSecret, 30);
    }

    private Application activeApplication() {
        return Application.reconstitute(applicationId, null, ApplicationSource.DIRECT,
                ApplicationStatus.APPROVED, LocalDateTime.now(), null, null, null, null, null, null);
    }

    @Test
    void login_validCredentials_returnsTokenAndStatus() {
        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(activeApplication()));
        when(verificationPort.verify(applicationId, last4SSN, dob)).thenReturn(true);

        ApplicantLoginResult result = useCase.execute(applicationId, last4SSN, dob);

        assertThat(result.sessionToken()).isNotBlank();
        assertThat(result.applicationId()).isEqualTo(applicationId);
        assertThat(result.applicationStatus()).isEqualTo("APPROVED");
        assertThat(result.expiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void login_applicationNotFound_throwsNotFoundException() {
        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(applicationId, last4SSN, dob))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    void login_verificationFails_throwsVerificationFailedException() {
        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(activeApplication()));
        when(verificationPort.verify(applicationId, last4SSN, dob)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(applicationId, last4SSN, dob))
                .isInstanceOf(ApplicantVerificationFailedException.class);
    }

    @Test
    void login_declinedApplication_throwsNotAccessibleException() {
        Application declined = Application.reconstitute(applicationId, null, ApplicationSource.DIRECT,
                ApplicationStatus.DECLINED, LocalDateTime.now(), null, null, null, null, null, null);
        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(declined));

        assertThatThrownBy(() -> useCase.execute(applicationId, last4SSN, dob))
                .isInstanceOf(ApplicationNotAccessibleException.class);
    }

    @Test
    void login_expiredApplication_throwsNotAccessibleException() {
        Application expired = Application.reconstitute(applicationId, null, ApplicationSource.DIRECT,
                ApplicationStatus.EXPIRED, LocalDateTime.now(), null, null, null, null, null, null);
        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> useCase.execute(applicationId, last4SSN, dob))
                .isInstanceOf(ApplicationNotAccessibleException.class);
    }
}
