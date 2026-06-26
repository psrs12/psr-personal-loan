package com.personalloan.applicationmanagement.infrastructure.external.ssn;

import com.personalloan.applicationmanagement.domain.application.SSNVerificationToken;
import com.personalloan.applicationmanagement.domain.application.port.SSNVerificationPort;
import com.personalloan.applicationmanagement.domain.exception.SSNVerificationFailedException;
import com.personalloan.applicationmanagement.infrastructure.external.ssn.dto.SSNVerifyRequest;
import com.personalloan.applicationmanagement.infrastructure.external.ssn.dto.SSNVerifyResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SSNVerificationAdapter implements SSNVerificationPort {

    private final RestClient restClient;

    public SSNVerificationAdapter(RestClient ssnVerificationRestClient) {
        this.restClient = ssnVerificationRestClient;
    }

    @Override
    @CircuitBreaker(name = "ssnVerification", fallbackMethod = "verifySSNFallback")
    public SSNVerificationToken verifySSN(String ssnToken) {
        SSNVerifyResponse response = restClient.post()
                .uri("/ssn/verify")
                .body(new SSNVerifyRequest(ssnToken))
                .retrieve()
                .onStatus(status -> status.value() == 422, (request, resp) -> {
                    throw new SSNVerificationFailedException();
                })
                .onStatus(status -> status.is4xxClientError() && status.value() != 422, (request, resp) -> {
                    throw new SSNVerificationFailedException();
                })
                .onStatus(status -> status.is5xxServerError(), (request, resp) -> {
                    throw new SSNVerificationFailedException();
                })
                .body(SSNVerifyResponse.class);

        if (response == null) {
            throw new SSNVerificationFailedException();
        }

        return new SSNVerificationToken(response.verificationToken(), response.expiresAt());
    }

    SSNVerificationToken verifySSNFallback(String ssnToken, Exception ex) {
        throw new SSNVerificationFailedException();
    }
}
