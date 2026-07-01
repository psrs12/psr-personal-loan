package com.personalloan.applicationmanagement.infrastructure.external.bolt;

import com.personalloan.applicationmanagement.domain.application.port.BoltTokenizationPort;
import com.personalloan.applicationmanagement.domain.exception.TokenizationUnavailableException;
import com.personalloan.applicationmanagement.infrastructure.external.bolt.dto.BoltTokenizeRequest;
import com.personalloan.applicationmanagement.infrastructure.external.bolt.dto.BoltTokenizeResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BoltTokenizationAdapter implements BoltTokenizationPort {

    private final RestClient restClient;

    public BoltTokenizationAdapter(RestClient boltRestClient) {
        this.restClient = boltRestClient;
    }

    @Override
    @CircuitBreaker(name = "bolt", fallbackMethod = "tokenizeFallback")
    @Retry(name = "bolt")
    public String tokenize(String ssn) {
        BoltTokenizeResponse response = restClient.post()
                .uri("/tokenize")
                .body(new BoltTokenizeRequest(ssn))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, resp) -> {
                    throw new TokenizationUnavailableException();
                })
                .onStatus(status -> status.is5xxServerError(), (request, resp) -> {
                    throw new TokenizationUnavailableException();
                })
                .body(BoltTokenizeResponse.class);

        if (response == null || response.token() == null) {
            throw new TokenizationUnavailableException();
        }

        return response.token();
    }

    String tokenizeFallback(String ssn, Exception ex) {
        // Stub token when BOLT is unavailable — last 4 digits only, never store raw SSN
        return "STUB-" + (ssn != null && ssn.length() >= 4 ? ssn.substring(ssn.length() - 4) : "0000");
    }
}
