package com.personalloan.applicationmanagement.api.pricing;

import com.personalloan.applicationmanagement.api.BaseAcceptanceTest;
import com.personalloan.applicationmanagement.domain.pricing.PricingOffer;
import com.personalloan.applicationmanagement.domain.pricing.PricingOfferStatus;
import com.personalloan.applicationmanagement.domain.pricing.port.PricingOfferRepository;
import com.personalloan.applicationmanagement.infrastructure.persistence.application.ApplicationJpaRepository;
import com.personalloan.applicationmanagement.infrastructure.persistence.application.ApplicationJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PricingAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private PricingOfferRepository pricingOfferRepository;

    @Autowired
    private ApplicationJpaRepository applicationJpaRepository;

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private UUID createApplicationInDb() {
        UUID applicationId = UUID.randomUUID();
        ApplicationJpaEntity entity = new ApplicationJpaEntity();
        entity.setApplicationId(applicationId);
        entity.setApplicationSource("DIRECT");
        entity.setApplicationStatus("OFFER_PENDING");
        entity.setCreatedTimestamp(LocalDateTime.now());
        applicationJpaRepository.save(entity);
        return applicationId;
    }

    private PricingOffer createActiveOffer(UUID applicationId) {
        PricingOffer offer = PricingOffer.create(
                applicationId,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(0.0599),
                BigDecimal.valueOf(0.0620),
                36,
                BigDecimal.valueOf(303.50),
                BigDecimal.valueOf(10926),
                LocalDateTime.now().plusDays(7),
                "model-v1",
                "bureau-snap-1");
        return pricingOfferRepository.save(offer);
    }

    private PricingOffer createExpiredOffer(UUID applicationId) {
        PricingOffer offer = PricingOffer.create(
                applicationId,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(0.0599),
                BigDecimal.valueOf(0.0620),
                36,
                BigDecimal.valueOf(303.50),
                BigDecimal.valueOf(10926),
                LocalDateTime.now().minusDays(1),
                "model-v1",
                "bureau-snap-1");
        return pricingOfferRepository.save(offer);
    }

    @Test
    void getPricingOffers_activeOfferExists_returnsOffers() {
        UUID applicationId = createApplicationInDb();
        createActiveOffer(applicationId);

        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                baseUrl("/applications/" + applicationId + "/pricing-offers"),
                Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getPricingOffers_onlyExpiredOffers_returnsEmpty() {
        UUID applicationId = createApplicationInDb();
        createExpiredOffer(applicationId);

        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                baseUrl("/applications/" + applicationId + "/pricing-offers"),
                Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getPricingOffers_applicationNotFound_returns404() {
        ResponseEntity<Object> response = restTemplate.getForEntity(
                baseUrl("/applications/" + UUID.randomUUID() + "/pricing-offers"),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void selectOffer_validOffer_returns200() {
        UUID applicationId = createApplicationInDb();
        PricingOffer offer = createActiveOffer(applicationId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("selectedPricingOfferId", offer.getPricingOfferId().toString()),
                jsonHeaders());

        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl("/applications/" + applicationId + "/offer-selection"),
                request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void selectOffer_expiredOffer_returns422() {
        UUID applicationId = createApplicationInDb();
        PricingOffer offer = createExpiredOffer(applicationId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("selectedPricingOfferId", offer.getPricingOfferId().toString()),
                jsonHeaders());

        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl("/applications/" + applicationId + "/offer-selection"),
                request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void selectOffer_offerNotFound_returns404() {
        UUID applicationId = createApplicationInDb();

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("selectedPricingOfferId", UUID.randomUUID().toString()),
                jsonHeaders());

        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl("/applications/" + applicationId + "/offer-selection"),
                request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void captureConsent_validRequest_returns200AndTransitionsState() {
        UUID applicationId = createApplicationInDb();
        PricingOffer offer = createActiveOffer(applicationId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of(
                        "selectedPricingOfferId", offer.getPricingOfferId().toString(),
                        "consentChannel", "WEB",
                        "applicantReference", "applicant-ref-001"),
                jsonHeaders());

        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl("/applications/" + applicationId + "/consent"),
                request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApplicationJpaEntity updatedApp = applicationJpaRepository.findById(applicationId).orElseThrow();
        assertThat(updatedApp.getApplicationStatus()).isEqualTo("CONSENT_CAPTURED");
    }

    @Test
    void captureConsent_missingChannel_returns400() {
        UUID applicationId = createApplicationInDb();
        PricingOffer offer = createActiveOffer(applicationId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("selectedPricingOfferId", offer.getPricingOfferId().toString()),
                jsonHeaders());

        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl("/applications/" + applicationId + "/consent"),
                request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
