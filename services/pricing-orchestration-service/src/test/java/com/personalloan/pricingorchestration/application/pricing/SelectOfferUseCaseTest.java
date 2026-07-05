package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.OfferSelection;
import com.personalloan.pricingorchestration.domain.pricing.PricingOffer;
import com.personalloan.pricingorchestration.domain.pricing.PricingOfferStatus;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.OfferSelectionRepository;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectOfferUseCaseTest {

    @Mock private PricingOfferRepository pricingOfferRepository;
    @Mock private OfferSelectionRepository offerSelectionRepository;
    @Mock private ApplicationManagementPort applicationManagementPort;

    private SelectOfferUseCase useCase;

    private final UUID applicationId = UUID.randomUUID();
    private final UUID pricingOfferId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new SelectOfferUseCase(pricingOfferRepository, offerSelectionRepository, applicationManagementPort);
    }

    private PricingOffer offer(PricingOfferStatus status, LocalDateTime offerExpiryDate) {
        return PricingOffer.reconstitute(pricingOfferId, applicationId, new BigDecimal("10000"),
                new BigDecimal("5.5"), new BigDecimal("6.1"), 36, new BigDecimal("300"),
                new BigDecimal("10800"), offerExpiryDate, "model-ref", "bureau-ref",
                status, LocalDateTime.now().minusDays(1));
    }

    @Test
    void execute_happyPath_persistsSelectionAndRecordsAuditEvent() {
        PricingOffer activeOffer = offer(PricingOfferStatus.ACTIVE, LocalDateTime.now().plusDays(5));
        when(pricingOfferRepository.findById(pricingOfferId)).thenReturn(Optional.of(activeOffer));
        when(offerSelectionRepository.save(any(OfferSelection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SelectOfferCommand command = new SelectOfferCommand(applicationId, pricingOfferId);
        OfferSelection result = useCase.execute(command);

        assertThat(result.getApplicationId()).isEqualTo(applicationId);
        assertThat(result.getSelectedPricingOfferId()).isEqualTo(pricingOfferId);
        verify(offerSelectionRepository).save(any(OfferSelection.class));
        verify(applicationManagementPort).recordAuditEvent(eq(applicationId), eq("OFFER_SELECTED"), anyString());
    }

    @Test
    void execute_offerNotFound_throwsPricingOfferNotFoundException() {
        when(pricingOfferRepository.findById(pricingOfferId)).thenReturn(Optional.empty());

        SelectOfferCommand command = new SelectOfferCommand(applicationId, pricingOfferId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(PricingOfferNotFoundException.class);

        verifyNoInteractions(offerSelectionRepository);
        verifyNoInteractions(applicationManagementPort);
    }

    @Test
    void execute_offerExpired_throwsPricingOfferExpiredException_doesNotSaveOrAudit() {
        PricingOffer expiredOffer = offer(PricingOfferStatus.ACTIVE, LocalDateTime.now().minusDays(1));
        when(pricingOfferRepository.findById(pricingOfferId)).thenReturn(Optional.of(expiredOffer));

        SelectOfferCommand command = new SelectOfferCommand(applicationId, pricingOfferId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(PricingOfferExpiredException.class);

        verifyNoInteractions(offerSelectionRepository);
        verifyNoInteractions(applicationManagementPort);
    }
}
