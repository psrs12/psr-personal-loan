package com.personalloan.offeracceptance.application;

import com.personalloan.offeracceptance.application.offer.ESignUseCase;
import com.personalloan.offeracceptance.domain.exception.AlreadySignedException;
import com.personalloan.offeracceptance.domain.exception.MandatoryDeclarationMissingException;
import com.personalloan.offeracceptance.domain.exception.SessionNotFoundException;
import com.personalloan.offeracceptance.domain.offer.OfferAcceptanceSession;
import com.personalloan.offeracceptance.domain.offer.SessionStatus;
import com.personalloan.offeracceptance.domain.port.ESignRecordRepository;
import com.personalloan.offeracceptance.domain.port.OfferAcceptanceEventPublisher;
import com.personalloan.offeracceptance.domain.port.OfferAcceptanceSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ESignUseCaseTest {

    @Mock OfferAcceptanceSessionRepository sessionRepository;
    @Mock ESignRecordRepository eSignRecordRepository;
    @Mock OfferAcceptanceEventPublisher eventPublisher;

    @InjectMocks ESignUseCase useCase;

    private final UUID applicationId = UUID.randomUUID();

    @Test
    void throwsWhenSessionNotFound() {
        when(sessionRepository.findByApplicationId(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(applicationId, Set.of(), "127.0.0.1"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void throwsWhenMandatoryDeclarationMissing() {
        OfferAcceptanceSession session = OfferAcceptanceSession.create(applicationId);
        when(sessionRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.execute(applicationId, Set.of(), "127.0.0.1"))
                .isInstanceOf(MandatoryDeclarationMissingException.class);
    }

    @Test
    void throwsWhenAlreadySigned() {
        OfferAcceptanceSession session = OfferAcceptanceSession.create(applicationId);
        Set<UUID> allMandatory = session.getDeclarations().stream()
                .filter(d -> d.mandatory())
                .map(d -> d.declarationId())
                .collect(Collectors.toSet());

        when(sessionRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(session));

        useCase.execute(applicationId, allMandatory, "127.0.0.1");

        when(sessionRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(
                OfferAcceptanceSession.reconstitute(
                        session.getSessionId(), applicationId,
                        OfferAcceptanceSession.STANDARD_DECLARATIONS,
                        SessionStatus.SIGNED, LocalDateTime.now())));

        assertThatThrownBy(() -> useCase.execute(applicationId, allMandatory, "127.0.0.1"))
                .isInstanceOf(AlreadySignedException.class);
    }

    @Test
    void signsSuccessfullyAndPublishesEvent() {
        OfferAcceptanceSession session = OfferAcceptanceSession.create(applicationId);
        Set<UUID> allMandatory = session.getDeclarations().stream()
                .filter(d -> d.mandatory())
                .map(d -> d.declarationId())
                .collect(Collectors.toSet());

        when(sessionRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(session));

        useCase.execute(applicationId, allMandatory, "127.0.0.1");

        verify(eSignRecordRepository).save(any());
        verify(sessionRepository).save(any());
        verify(eventPublisher).publishESignCompleted(eq(applicationId), any(), any());
    }
}
