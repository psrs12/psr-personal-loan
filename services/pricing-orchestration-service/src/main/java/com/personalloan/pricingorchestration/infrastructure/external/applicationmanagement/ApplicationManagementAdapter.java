package com.personalloan.pricingorchestration.infrastructure.external.applicationmanagement;

import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.infrastructure.external.applicationmanagement.dto.ApplicationDataResponse;
import com.personalloan.pricingorchestration.infrastructure.external.applicationmanagement.dto.ApplicationStatusRequest;
import com.personalloan.pricingorchestration.infrastructure.external.applicationmanagement.dto.CreditReferenceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class ApplicationManagementAdapter implements ApplicationManagementPort {

    private final RestClient restClient;

    @Autowired
    public ApplicationManagementAdapter(RestClient.Builder builder,
                                         @Value("${integration.application-management.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public PricingRequest assembleFromApplicationData(UUID applicationId) {
        ApplicationDataResponse data = restClient.get()
                .uri("/api/v1/application-management/internal/applications/{id}/pricing-data", applicationId)
                .retrieve()
                .body(ApplicationDataResponse.class);
        return new PricingRequest(
                applicationId,
                data.softPullCreditReportReferenceId(),
                data.requestedAmount(),
                data.requestedTermMonths(),
                data.loanPurpose(),
                data.annualIncome(),
                data.employmentStatus(),
                data.campaignOfferId(),
                data.campaignOfferTerms()
        );
    }

    @Override
    public void updateApplicationStatus(UUID applicationId, String status) {
        restClient.patch()
                .uri("/api/v1/application-management/internal/applications/{id}/status", applicationId)
                .body(new ApplicationStatusRequest(status))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void persistSoftPullReference(UUID applicationId, String creditReportReferenceId) {
        restClient.patch()
                .uri("/api/v1/application-management/internal/applications/{id}/soft-pull-reference", applicationId)
                .body(new CreditReferenceRequest(creditReportReferenceId))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void persistHardPullReference(UUID applicationId, String creditReportReferenceId) {
        restClient.patch()
                .uri("/api/v1/application-management/internal/applications/{id}/hard-pull-reference", applicationId)
                .body(new CreditReferenceRequest(creditReportReferenceId))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void persistPricingOffers(UUID applicationId, Object offersPayload) {
        restClient.post()
                .uri("/api/v1/application-management/internal/applications/{id}/pricing-offers", applicationId)
                .body(offersPayload)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void markOffersSuperseded(UUID applicationId) {
        restClient.patch()
                .uri("/api/v1/application-management/internal/applications/{id}/offers/supersede", applicationId)
                .retrieve()
                .toBodilessEntity();
    }
}
