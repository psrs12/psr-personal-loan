package com.personalloan.applicationmanagement.infrastructure.external.customer;

import com.personalloan.applicationmanagement.domain.invitation.CustomerPrefill;
import com.personalloan.applicationmanagement.domain.invitation.port.CustomerProfilePort;
import com.personalloan.applicationmanagement.infrastructure.external.customer.dto.CustomerProfileResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class CustomerProfileAdapter implements CustomerProfilePort {

    private final RestClient restClient;

    public CustomerProfileAdapter(RestClient customerProfileRestClient) {
        this.restClient = customerProfileRestClient;
    }

    @Override
    @CircuitBreaker(name = "customerProfile", fallbackMethod = "retrieveCustomerFallback")
    public Optional<CustomerPrefill> retrieveCustomer(String customerReferenceId) {
        CustomerProfileResponse response = restClient.get()
                .uri("/customers/{id}", customerReferenceId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, resp) -> {
                    // not found — return empty
                })
                .onStatus(status -> status.value() != 404 && status.is4xxClientError(), (request, resp) -> {
                    throw new RuntimeException("Customer Profile client error: " + resp.getStatusCode().value());
                })
                .onStatus(status -> status.is5xxServerError(), (request, resp) -> {
                    throw new RuntimeException("Customer Profile service unavailable");
                })
                .body(CustomerProfileResponse.class);

        return Optional.ofNullable(response).map(r -> new CustomerPrefill(
                r.firstName(),
                r.lastName(),
                r.street(),
                r.city(),
                r.state(),
                r.zip()
        ));
    }

    Optional<CustomerPrefill> retrieveCustomerFallback(String customerReferenceId, Exception ex) {
        return Optional.empty();
    }
}
