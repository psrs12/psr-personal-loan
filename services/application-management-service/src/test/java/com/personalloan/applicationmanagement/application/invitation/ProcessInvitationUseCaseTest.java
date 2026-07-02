package com.personalloan.applicationmanagement.application.invitation;

import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.exception.DuplicateApplicationException;
import com.personalloan.applicationmanagement.domain.exception.OfferExpiredException;
import com.personalloan.applicationmanagement.domain.invitation.*;
import com.personalloan.applicationmanagement.domain.invitation.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessInvitationUseCaseTest {

    @Mock private OfferManagementPort offerManagementPort;
    @Mock private CustomerProfilePort customerProfilePort;
    @Mock private InvitationSessionRepository invitationSessionRepository;
    @Mock private ApplicationIntakeContextRepository applicationIntakeContextRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationAuditRepository applicationAuditRepository;

    private ProcessInvitationUseCase useCase;

    private final OfferDetails validOffer = new OfferDetails(
            "offer-001", "cref-001", new BigDecimal("10000"), new BigDecimal("5.5"),
            36, LocalDate.now().plusYears(1)
    );

    private final CustomerPrefill customerPrefill = new CustomerPrefill(
            "John", "Doe", "123 Main St", "Springfield", "IL", "62701"
    );

    @BeforeEach
    void setUp() {
        useCase = new ProcessInvitationUseCase(
                offerManagementPort, customerProfilePort,
                invitationSessionRepository, applicationIntakeContextRepository,
                applicationRepository, applicationAuditRepository
        );
        ReflectionTestUtils.setField(useCase, "sessionExpirationMinutes", 30);
    }

    @Test
    void execute_happyPath_returnsCompletePrefillResult() {
        when(applicationRepository.existsActiveApplicationForInvitation("inv-001")).thenReturn(false);
        when(offerManagementPort.retrieveOffer("inv-001")).thenReturn(validOffer);
        when(customerProfilePort.retrieveCustomer("cref-001")).thenReturn(Optional.of(customerPrefill));
        when(invitationSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applicationIntakeContextRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProcessInvitationResult result = useCase.execute("inv-001");

        assertThat(result.offerDetails()).isEqualTo(validOffer);
        assertThat(result.customerPrefill()).isEqualTo(customerPrefill);
        assertThat(result.prefillStatus()).isEqualTo(PrefillStatus.COMPLETE);
        assertThat(result.intakeId()).isNotNull();
        verify(invitationSessionRepository, atLeast(2)).save(any());
    }

    @Test
    void execute_customerLookupFails_returnsPartialPrefill() {
        when(applicationRepository.existsActiveApplicationForInvitation("inv-001")).thenReturn(false);
        when(offerManagementPort.retrieveOffer("inv-001")).thenReturn(validOffer);
        when(customerProfilePort.retrieveCustomer("cref-001")).thenThrow(new RuntimeException("unavailable"));
        when(invitationSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applicationIntakeContextRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProcessInvitationResult result = useCase.execute("inv-001");

        assertThat(result.prefillStatus()).isEqualTo(PrefillStatus.PARTIAL);
        assertThat(result.customerPrefill()).isNull();
    }

    @Test
    void execute_duplicateActiveApplication_throwsDuplicateApplicationException() {
        when(applicationRepository.existsActiveApplicationForInvitation("inv-001")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("inv-001"))
                .isInstanceOf(DuplicateApplicationException.class);

        verifyNoInteractions(offerManagementPort, invitationSessionRepository);
    }

    @Test
    void execute_expiredOffer_throwsOfferExpiredException() {
        OfferDetails expiredOffer = new OfferDetails(
                "offer-expired", "cref-001", new BigDecimal("10000"), new BigDecimal("5.5"),
                36, LocalDate.now().minusDays(1)
        );
        when(applicationRepository.existsActiveApplicationForInvitation("inv-001")).thenReturn(false);
        when(offerManagementPort.retrieveOffer("inv-001")).thenReturn(expiredOffer);
        when(invitationSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> useCase.execute("inv-001"))
                .isInstanceOf(OfferExpiredException.class);
    }
}
