package com.personalloan.applicationmanagement.api.invitation;

import com.personalloan.applicationmanagement.api.BaseAcceptanceTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class InvitationAcceptanceTest extends BaseAcceptanceTest {

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer test-token");
        h.set("X-Channel-ID", "WEB");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private void stubValidOffer(String invitationId) {
        offerManagementMock.stubFor(get(urlEqualTo("/invitations/" + invitationId + "/validate"))
                .willReturn(aResponse().withStatus(200)));
        offerManagementMock.stubFor(get(urlEqualTo("/invitations/" + invitationId + "/offer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "offerId": "offer-001",
                                  "customerReferenceId": "cref-001",
                                  "loanAmount": 10000.00,
                                  "apr": 5.5,
                                  "termMonths": 36,
                                  "expirationDate": "2027-12-31"
                                }
                                """)));
    }

    private void stubCustomerProfile(String customerReferenceId) {
        customerProfileMock.stubFor(get(urlEqualTo("/customers/" + customerReferenceId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "customerReferenceId": "cref-001",
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "street": "123 Main St",
                                  "city": "Springfield",
                                  "state": "IL",
                                  "zip": "62701"
                                }
                                """)));
    }

    @Test
    void initializeInvitation_validITA_returnsPrefillWithOfferAndName() {
        stubValidOffer("inv-001");
        stubCustomerProfile("cref-001");

        var body = Map.of("invitationId", "inv-001");
        var request = new HttpEntity<>(body, headers());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/invitations/initialize"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("intakeId");
        assertThat(response.getBody()).containsKey("offer");
        assertThat(response.getBody()).containsKey("prefill");

        @SuppressWarnings("unchecked")
        Map<String, Object> prefill = (Map<String, Object>) response.getBody().get("prefill");
        assertThat(prefill).containsKey("firstName");
        assertThat(prefill).containsKey("lastName");
        assertThat(prefill).doesNotContainKey("phone");
        assertThat(prefill).doesNotContainKey("email");

        assertThat(response.getBody().get("prefillStatus")).isEqualTo("COMPLETE");
    }

    @Test
    void initializeInvitation_invitationNotFound_returnsNotFound() {
        offerManagementMock.stubFor(get(urlEqualTo("/invitations/inv-999/validate"))
                .willReturn(aResponse().withStatus(404)));

        var body = Map.of("invitationId", "inv-999");
        var request = new HttpEntity<>(body, headers());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/invitations/initialize"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("errorCode")).isEqualTo("INVITATION_NOT_FOUND");
    }

    @Test
    void initializeInvitation_invitationExpired_returns422() {
        offerManagementMock.stubFor(get(urlEqualTo("/invitations/inv-exp/validate"))
                .willReturn(aResponse().withStatus(410)));

        var body = Map.of("invitationId", "inv-exp");
        var request = new HttpEntity<>(body, headers());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/invitations/initialize"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("errorCode")).isEqualTo("INVITATION_EXPIRED");
    }

    @Test
    void initializeInvitation_customerLookupFails_returnsPartialPrefill() {
        stubValidOffer("inv-partial");
        customerProfileMock.stubFor(get(urlEqualTo("/customers/cref-001"))
                .willReturn(aResponse().withStatus(503)));

        var body = Map.of("invitationId", "inv-partial");
        var request = new HttpEntity<>(body, headers());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/invitations/initialize"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("prefillStatus")).isEqualTo("PARTIAL");
    }

    @Test
    void initializeInvitation_offerManagementUnavailable_returns503() {
        offerManagementMock.stubFor(get(urlEqualTo("/invitations/inv-down/validate"))
                .willReturn(aResponse().withStatus(500)));

        var body = Map.of("invitationId", "inv-down");
        var request = new HttpEntity<>(body, headers());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/invitations/initialize"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().get("errorCode")).isEqualTo("OFFER_UNAVAILABLE");
    }
}
