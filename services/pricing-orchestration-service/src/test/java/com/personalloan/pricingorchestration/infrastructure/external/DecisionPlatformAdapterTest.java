package com.personalloan.pricingorchestration.infrastructure.external;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.personalloan.pricingorchestration.domain.pricing.FinalDecisionResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingEngineResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;
import com.personalloan.pricingorchestration.infrastructure.external.decisionplatform.DecisionPlatformAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class DecisionPlatformAdapterTest {

    private WireMockServer wireMock;
    private DecisionPlatformAdapter adapter;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(10000);

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .requestFactory(factory)
                .build();

        adapter = new DecisionPlatformAdapter(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    private PricingRequest buildPricingRequest(UUID applicationId) {
        return new PricingRequest(applicationId, "soft-ref-001",
                BigDecimal.valueOf(10000), 36, "DEBT_CONSOLIDATION",
                BigDecimal.valueOf(60000), "EMPLOYED", null, null);
    }

    @Test
    void requestPricing_offersGenerated_returnsPricingOffers() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/pricing/evaluate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "outcome": "OFFERS_GENERATED",
                                  "offers": [
                                    {
                                      "pricingOfferId": "po-001",
                                      "approvedAmount": 10000.00,
                                      "interestRate": 0.0599,
                                      "apr": 0.0620,
                                      "termMonths": 36,
                                      "monthlyRepayment": 303.50,
                                      "totalRepayable": 10926.00,
                                      "offerExpiryDate": "2026-07-03T00:00:00",
                                      "pricingModelRef": "model-v1",
                                      "bureauSnapshotRef": "snap-001"
                                    }
                                  ],
                                  "declineReasonCode": null
                                }
                                """)));

        PricingEngineResponse response = adapter.requestPricing(buildPricingRequest(applicationId));

        assertThat(response.outcome()).isEqualTo(PricingEngineResponse.PricingOutcome.OFFERS_GENERATED);
        assertThat(response.offers()).hasSize(1);
        assertThat(response.offers().get(0).pricingOfferId()).isEqualTo("po-001");
        assertThat(response.offers().get(0).approvedAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
    }

    @Test
    void requestPricing_declined_returnsDeclineWithReasonCode() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/pricing/evaluate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "outcome": "DECLINED",
                                  "offers": [],
                                  "declineReasonCode": "CREDIT_SCORE_TOO_LOW"
                                }
                                """)));

        PricingEngineResponse response = adapter.requestPricing(buildPricingRequest(applicationId));

        assertThat(response.outcome()).isEqualTo(PricingEngineResponse.PricingOutcome.DECLINED);
        assertThat(response.declineReasonCode()).isEqualTo("CREDIT_SCORE_TOO_LOW");
        assertThat(response.offers()).isEmpty();
    }

    @Test
    void requestPricing_itaJourney_includesCampaignOffer() {
        UUID applicationId = UUID.randomUUID();
        PricingRequest itaRequest = new PricingRequest(applicationId, "soft-ref-001",
                BigDecimal.valueOf(10000), 36, "DEBT_CONSOLIDATION",
                BigDecimal.valueOf(60000), "EMPLOYED", "campaign-offer-99", "{\"apr\":5.5}");

        wireMock.stubFor(post(urlEqualTo("/pricing/evaluate"))
                .withRequestBody(containing("campaign-offer-99"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "outcome": "OFFERS_GENERATED",
                                  "offers": [],
                                  "declineReasonCode": null
                                }
                                """)));

        PricingEngineResponse response = adapter.requestPricing(itaRequest);

        assertThat(response.outcome()).isEqualTo(PricingEngineResponse.PricingOutcome.OFFERS_GENERATED);
        wireMock.verify(postRequestedFor(urlEqualTo("/pricing/evaluate"))
                .withRequestBody(containing("campaign-offer-99")));
    }

    @Test
    void requestPricing_serverError_throwsException() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/pricing/evaluate"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.requestPricing(buildPricingRequest(applicationId)))
                .isInstanceOf(Exception.class);
    }

    @Test
    void requestFinalDecision_approved_returnsApprovedOutcome() {
        UUID applicationId = UUID.randomUUID();
        UUID selectedOfferId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/decision/evaluate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "outcome": "APPROVED",
                                  "reasonCode": null
                                }
                                """)));

        FinalDecisionResponse response = adapter.requestFinalDecision(applicationId, selectedOfferId, "hard-ref-001");

        assertThat(response.outcome()).isEqualTo(FinalDecisionResponse.DecisionOutcome.APPROVED);
    }

    @Test
    void requestFinalDecision_declined_returnsDeclinedOutcomeWithReason() {
        UUID applicationId = UUID.randomUUID();
        UUID selectedOfferId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/decision/evaluate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "outcome": "DECLINED",
                                  "reasonCode": "HIGH_DTI"
                                }
                                """)));

        FinalDecisionResponse response = adapter.requestFinalDecision(applicationId, selectedOfferId, "hard-ref-001");

        assertThat(response.outcome()).isEqualTo(FinalDecisionResponse.DecisionOutcome.DECLINED);
        assertThat(response.reasonCode()).isEqualTo("HIGH_DTI");
    }

    @Test
    void requestFinalDecision_referred_returnsReferredOutcome() {
        UUID applicationId = UUID.randomUUID();
        UUID selectedOfferId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/decision/evaluate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "outcome": "REFERRED",
                                  "reasonCode": null
                                }
                                """)));

        FinalDecisionResponse response = adapter.requestFinalDecision(applicationId, selectedOfferId, "hard-ref-001");

        assertThat(response.outcome()).isEqualTo(FinalDecisionResponse.DecisionOutcome.REFERRED);
    }

    @Test
    void requestFinalDecision_serverError_throwsException() {
        UUID applicationId = UUID.randomUUID();
        UUID selectedOfferId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/decision/evaluate"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.requestFinalDecision(applicationId, selectedOfferId, "hard-ref-001"))
                .isInstanceOf(Exception.class);
    }
}
