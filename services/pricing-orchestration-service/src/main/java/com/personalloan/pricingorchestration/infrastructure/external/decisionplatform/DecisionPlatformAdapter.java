package com.personalloan.pricingorchestration.infrastructure.external.decisionplatform;

import com.personalloan.pricingorchestration.domain.pricing.FinalDecisionResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingEngineResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;
import com.personalloan.pricingorchestration.domain.pricing.port.DecisionPlatformPort;
import com.personalloan.pricingorchestration.infrastructure.external.decisionplatform.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class DecisionPlatformAdapter implements DecisionPlatformPort {

    private final RestClient restClient;

    @Autowired
    public DecisionPlatformAdapter(RestClient.Builder builder,
                                    @Value("${integration.decision-platform.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public DecisionPlatformAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @CircuitBreaker(name = "decisionPlatform")
    @Retry(name = "decisionPlatform")
    public PricingEngineResponse requestPricing(PricingRequest request) {
        PricingEngineApiResponse apiResponse = restClient.post()
                .uri("/pricing/evaluate")
                .body(new PricingEngineRequest(
                        request.applicationId(),
                        request.softPullCreditReportReferenceId(),
                        request.requestedAmount(),
                        request.requestedTermMonths(),
                        request.loanPurpose(),
                        request.annualIncome(),
                        request.employmentStatus(),
                        request.campaignOfferId(),
                        request.campaignOfferTerms()))
                .retrieve()
                .body(PricingEngineApiResponse.class);

        return toDomain(apiResponse);
    }

    @Override
    @CircuitBreaker(name = "decisionPlatform")
    @Retry(name = "decisionPlatform")
    public FinalDecisionResponse requestFinalDecision(UUID applicationId, UUID selectedPricingOfferId,
                                                       String hardPullCreditReportReferenceId) {
        FinalDecisionApiResponse apiResponse = restClient.post()
                .uri("/decision/evaluate")
                .body(new FinalDecisionRequest(applicationId, selectedPricingOfferId, hardPullCreditReportReferenceId))
                .retrieve()
                .body(FinalDecisionApiResponse.class);

        List<FinalDecisionResponse.DocumentCode> documents = apiResponse.documents() == null ? List.of() :
                apiResponse.documents().stream()
                        .map(d -> new FinalDecisionResponse.DocumentCode(d.decisionEngineCode(), d.count()))
                        .toList();

        return new FinalDecisionResponse(
                FinalDecisionResponse.DecisionOutcome.valueOf(apiResponse.outcome()),
                apiResponse.reasonCode(),
                documents);
    }

    private PricingEngineResponse toDomain(PricingEngineApiResponse api) {
        List<PricingEngineResponse.PricingOfferData> offers = api.offers() == null ? List.of() :
                api.offers().stream().map(o -> new PricingEngineResponse.PricingOfferData(
                        o.pricingOfferId(), o.approvedAmount(), o.interestRate(), o.apr(),
                        o.termMonths(), o.monthlyRepayment(), o.totalRepayable(),
                        o.offerExpiryDate(), o.pricingModelRef(), o.bureauSnapshotRef()
                )).toList();

        return new PricingEngineResponse(
                PricingEngineResponse.PricingOutcome.valueOf(api.outcome()),
                offers,
                api.declineReasonCode());
    }
}
