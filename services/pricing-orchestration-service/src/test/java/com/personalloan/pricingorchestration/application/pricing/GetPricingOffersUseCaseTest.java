package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.ApplicationExpiryInfo;
import com.personalloan.pricingorchestration.domain.pricing.PricingOffer;
import com.personalloan.pricingorchestration.domain.pricing.PricingOfferStatus;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPricingOffersUseCaseTest {

    @Mock private PricingOfferRepository pricingOfferRepository;
    @Mock private ApplicationManagementPort applicationManagementPort;

    private GetPricingOffersUseCase useCase;

    private final UUID applicationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetPricingOffersUseCase(pricingOfferRepository, applicationManagementPort);
    }

    private PricingOffer offer(PricingOfferStatus status, LocalDateTime offerExpiryDate) {
        return PricingOffer.reconstitute(UUID.randomUUID(), applicationId, new BigDecimal("10000"),
                new BigDecimal("5.5"), new BigDecimal("6.1"), 36, new BigDecimal("300"),
                new BigDecimal("10800"), offerExpiryDate, "model-ref", "bureau-ref",
                status, LocalDateTime.now().minusDays(1));
    }

    @Test
    void execute_notExpired_returnsActiveNonExpiredOffers() {
        when(applicationManagementPort.getApplicationExpiryInfo(applicationId))
                .thenReturn(new ApplicationExpiryInfo(LocalDateTime.now().plusDays(5), "PRICING_PENDING"));

        PricingOffer activeOffer = offer(PricingOfferStatus.ACTIVE, LocalDateTime.now().plusDays(10));
        when(pricingOfferRepository.findByApplicationId(applicationId)).thenReturn(List.of(activeOffer));

        List<PricingOffer> result = useCase.execute(applicationId);

        assertThat(result).containsExactly(activeOffer);
        verify(applicationManagementPort, never()).updateApplicationStatus(any(), anyString());
    }

    @Test
    void execute_filtersOutExpiredAndSupersededOffers() {
        when(applicationManagementPort.getApplicationExpiryInfo(applicationId))
                .thenReturn(new ApplicationExpiryInfo(LocalDateTime.now().plusDays(5), "PRICING_PENDING"));

        PricingOffer activeOffer = offer(PricingOfferStatus.ACTIVE, LocalDateTime.now().plusDays(10));
        PricingOffer expiredOffer = offer(PricingOfferStatus.ACTIVE, LocalDateTime.now().minusDays(1));
        PricingOffer supersededOffer = offer(PricingOfferStatus.SUPERSEDED, LocalDateTime.now().plusDays(10));
        when(pricingOfferRepository.findByApplicationId(applicationId))
                .thenReturn(List.of(activeOffer, expiredOffer, supersededOffer));

        List<PricingOffer> result = useCase.execute(applicationId);

        assertThat(result).containsExactly(activeOffer);
    }

    @Test
    void execute_applicationExpired_updatesStatusAndThrows_doesNotCallOfferRepository() {
        when(applicationManagementPort.getApplicationExpiryInfo(applicationId))
                .thenReturn(new ApplicationExpiryInfo(LocalDateTime.now().minusDays(1), "PRICING_PENDING"));

        assertThatThrownBy(() -> useCase.execute(applicationId))
                .isInstanceOf(ApplicationExpiredException.class);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "EXPIRED");
        verifyNoInteractions(pricingOfferRepository);
    }
}
