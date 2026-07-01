package com.personalloan.applicationmanagement.application.application;

import com.personalloan.applicationmanagement.domain.application.*;
import com.personalloan.applicationmanagement.domain.application.port.*;
import com.personalloan.applicationmanagement.domain.exception.IntakeExpiredException;
import com.personalloan.applicationmanagement.domain.exception.IntakeNotFoundException;
import com.personalloan.applicationmanagement.domain.exception.SSNVerificationTokenInvalidException;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationIntakeContext;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import com.personalloan.applicationmanagement.domain.invitation.InvitationSession;
import com.personalloan.applicationmanagement.domain.invitation.InvitationSessionStatus;
import com.personalloan.applicationmanagement.domain.invitation.PrefillStatus;
import com.personalloan.applicationmanagement.domain.invitation.port.ApplicationIntakeContextRepository;
import com.personalloan.applicationmanagement.domain.invitation.port.InvitationSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateApplicationUseCaseTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private LoanRequestRepository loanRequestRepository;
    @Mock private ApplicationOfferRepository applicationOfferRepository;
    @Mock private ApplicationAuditRepository applicationAuditRepository;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @Mock private ApplicationIntakeContextRepository applicationIntakeContextRepository;
    @Mock private InvitationSessionRepository invitationSessionRepository;
    @Mock private SSNTokenStore ssnTokenStore;
    @Mock private BoltTokenizationPort boltTokenizationPort;

    private CreateApplicationUseCase useCase;

    private final UUID intakeId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final String validToken = "valid-token";

    @BeforeEach
    void setUp() {
        useCase = new CreateApplicationUseCase(
                applicationRepository, applicantRepository, loanRequestRepository,
                applicationOfferRepository, applicationAuditRepository, applicationEventPublisher,
                applicationIntakeContextRepository, invitationSessionRepository,
                ssnTokenStore, boltTokenizationPort
        );
    }

    private CreateApplicationCommand itaCommand() {
        return new CreateApplicationCommand(
                intakeId, validToken, "John", "Doe", LocalDate.of(1990, 1, 1),
                Citizenship.US_CITIZEN, "123456789", "john@example.com", "555-1234",
                "123 Main St", null, "Springfield", "IL", "62701",
                "Acme Corp", EmploymentStatus.EMPLOYED, new BigDecimal("75000"),
                new BigDecimal("10000"), 36, "HOME_IMPROVEMENT"
        );
    }

    private CreateApplicationCommand directCommand() {
        return new CreateApplicationCommand(
                null, validToken, "Jane", "Doe", LocalDate.of(1985, 6, 15),
                Citizenship.US_CITIZEN, "987654321", "jane@example.com", "555-5678",
                "456 Oak Ave", null, "Chicago", "IL", "60601",
                null, EmploymentStatus.SELF_EMPLOYED, new BigDecimal("90000"),
                new BigDecimal("15000"), 48, null
        );
    }

    @Test
    void execute_itaPath_createsApplicationWithOffer() {
        ApplicationIntakeContext intakeContext = ApplicationIntakeContext.createForInvitation(
                sessionId, "inv-001", "offer-001", "cref-001", PrefillStatus.COMPLETE
        );
        InvitationSession session = InvitationSession.reconstitute(
                sessionId, "inv-001", ApplicationSource.INVITATION,
                "offer-001", "cref-001", InvitationSessionStatus.COMPLETED,
                LocalDateTime.now().minusMinutes(5), null, LocalDateTime.now().plusMinutes(25)
        );

        when(ssnTokenStore.isValid(validToken)).thenReturn(true);
        when(applicationIntakeContextRepository.findByIntakeId(intakeId)).thenReturn(Optional.of(intakeContext));
        when(invitationSessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(applicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(boltTokenizationPort.tokenize(anyString())).thenReturn("bolt-tok-123");

        UUID applicationId = useCase.execute(itaCommand());

        assertThat(applicationId).isNotNull();
        verify(boltTokenizationPort).tokenize("123456789");
        verify(applicationRepository).save(any());
        verify(applicantRepository).save(any());
        verify(loanRequestRepository).save(any());
        verify(applicationOfferRepository).save(any());
        verify(applicationEventPublisher).publishApplicationCreated(any());
        verify(ssnTokenStore).consume(validToken);
    }

    @Test
    void execute_directPath_createsApplicationWithoutOffer() {
        when(ssnTokenStore.isValid(validToken)).thenReturn(true);
        when(applicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(boltTokenizationPort.tokenize(anyString())).thenReturn("bolt-tok-456");

        UUID applicationId = useCase.execute(directCommand());

        assertThat(applicationId).isNotNull();
        verify(boltTokenizationPort).tokenize("987654321");
        verify(applicationRepository).save(any());
        verify(applicantRepository).save(any());
        verify(loanRequestRepository).save(any());
        verifyNoInteractions(applicationOfferRepository, applicationIntakeContextRepository);
        verify(applicationEventPublisher).publishApplicationCreated(any());
    }

    @Test
    void execute_invalidSsnToken_throwsSSNVerificationTokenInvalidException() {
        when(ssnTokenStore.isValid(validToken)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(itaCommand()))
                .isInstanceOf(SSNVerificationTokenInvalidException.class);

        verifyNoInteractions(applicationRepository, boltTokenizationPort);
    }

    @Test
    void execute_intakeNotFound_throwsIntakeNotFoundException() {
        when(ssnTokenStore.isValid(validToken)).thenReturn(true);
        when(boltTokenizationPort.tokenize(anyString())).thenReturn("bolt-tok-123");
        when(applicationIntakeContextRepository.findByIntakeId(intakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(itaCommand()))
                .isInstanceOf(IntakeNotFoundException.class);
    }

    @Test
    void execute_expiredSession_throwsIntakeExpiredException() {
        ApplicationIntakeContext intakeContext = ApplicationIntakeContext.createForInvitation(
                sessionId, "inv-001", "offer-001", "cref-001", PrefillStatus.COMPLETE
        );
        InvitationSession expiredSession = InvitationSession.reconstitute(
                sessionId, "inv-001", ApplicationSource.INVITATION,
                "offer-001", "cref-001", InvitationSessionStatus.COMPLETED,
                LocalDateTime.now().minusHours(2), null,
                LocalDateTime.now().minusMinutes(30)
        );

        when(ssnTokenStore.isValid(validToken)).thenReturn(true);
        when(boltTokenizationPort.tokenize(anyString())).thenReturn("bolt-tok-123");
        when(applicationIntakeContextRepository.findByIntakeId(intakeId)).thenReturn(Optional.of(intakeContext));
        when(invitationSessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(expiredSession));

        assertThatThrownBy(() -> useCase.execute(itaCommand()))
                .isInstanceOf(IntakeExpiredException.class);
    }
}
