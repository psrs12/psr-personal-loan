package com.personalloan.applicationmanagement.infrastructure.external.offer;

import com.personalloan.applicationmanagement.domain.exception.InvitationExpiredException;
import com.personalloan.applicationmanagement.domain.exception.InvitationNotFoundException;
import com.personalloan.applicationmanagement.domain.exception.OfferUnavailableException;
import com.personalloan.applicationmanagement.domain.invitation.OfferDetails;
import com.personalloan.applicationmanagement.domain.invitation.port.OfferManagementPort;
import com.personalloan.applicationmanagement.infrastructure.external.offer.dto.RetrieveOfferResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OfferManagementAdapter implements OfferManagementPort {

    private final RestClient restClient;

    public OfferManagementAdapter(RestClient offerManagementRestClient) {
        this.restClient = offerManagementRestClient;
    }

    @Override
    @CircuitBreaker(name = "offerManagement", fallbackMethod = "validateInvitationFallback")
    @Retry(name = "offerManagement")
    public void validateInvitation(String invitationId) {
        restClient.get()
                .uri("/invitations/{id}/validate", invitationId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    int status = response.getStatusCode().value();
                    if (status == 404) throw new InvitationNotFoundException(invitationId);
                    if (status == 410) throw new InvitationExpiredException(invitationId);
                    throw new OfferUnavailableException("Unexpected client error: " + status);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new OfferUnavailableException("Offer Management Platform unavailable");
                })
                .toBodilessEntity();
    }

    @Override
    @CircuitBreaker(name = "offerManagement", fallbackMethod = "retrieveOfferFallback")
    @Retry(name = "offerManagement")
    public OfferDetails retrieveOffer(String invitationId) {
        RetrieveOfferResponse response = restClient.get()
                .uri("/invitations/{id}/offer", invitationId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, resp) -> {
                    int status = resp.getStatusCode().value();
                    if (status == 404) throw new InvitationNotFoundException(invitationId);
                    if (status == 410) throw new InvitationExpiredException(invitationId);
                    throw new OfferUnavailableException("Unexpected client error: " + status);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, resp) -> {
                    throw new OfferUnavailableException("Offer Management Platform unavailable");
                })
                .body(RetrieveOfferResponse.class);

        if (response == null) {
            throw new OfferUnavailableException("Empty response from Offer Management Platform");
        }

        return new OfferDetails(
                response.offerId(),
                response.customerReferenceId(),
                response.loanAmount(),
                response.apr(),
                response.termMonths(),
                response.expirationDate()
        );
    }

    void validateInvitationFallback(String invitationId, Exception ex) {
        if (ex instanceof InvitationNotFoundException || ex instanceof InvitationExpiredException) {
            throw (RuntimeException) ex;
        }
        throw new OfferUnavailableException("Offer Management Platform circuit open");
    }

    OfferDetails retrieveOfferFallback(String invitationId, Exception ex) {
        if (ex instanceof InvitationNotFoundException || ex instanceof InvitationExpiredException) {
            throw (RuntimeException) ex;
        }
        throw new OfferUnavailableException("Offer Management Platform circuit open");
    }
}
