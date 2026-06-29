package com.personalloan.pricingorchestration.infrastructure.external.creditmanagement;

import com.personalloan.pricingorchestration.domain.pricing.port.CreditManagementPort;
import com.personalloan.pricingorchestration.infrastructure.external.creditmanagement.dto.CreditPullResponse;
import com.personalloan.pricingorchestration.infrastructure.external.creditmanagement.dto.HardPullRequest;
import com.personalloan.pricingorchestration.infrastructure.external.creditmanagement.dto.SoftPullRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class CreditManagementAdapter implements CreditManagementPort {

    private final RestClient restClient;

    public CreditManagementAdapter(RestClient.Builder builder,
                                    @Value("${integration.credit-management.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    CreditManagementAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @CircuitBreaker(name = "creditManagement")
    @Retry(name = "creditManagement")
    public String initiateSoftPull(UUID applicationId, String applicantReference) {
        CreditPullResponse response = restClient.post()
                .uri("/credit/soft-pull")
                .body(new SoftPullRequest(applicationId, applicantReference))
                .retrieve()
                .body(CreditPullResponse.class);
        return response.creditReportReferenceId();
    }

    @Override
    @CircuitBreaker(name = "creditManagement")
    @Retry(name = "creditManagement")
    public String initiateHardPull(UUID applicationId, String applicantReference, String softPullReferenceId) {
        CreditPullResponse response = restClient.post()
                .uri("/credit/hard-pull")
                .body(new HardPullRequest(applicationId, applicantReference, softPullReferenceId))
                .retrieve()
                .body(CreditPullResponse.class);
        return response.creditReportReferenceId();
    }
}
