package com.personalloan.applicationmanagement.infrastructure.external.offer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.personalloan.applicationmanagement.domain.exception.InvitationExpiredException;
import com.personalloan.applicationmanagement.domain.exception.InvitationNotFoundException;
import com.personalloan.applicationmanagement.domain.exception.OfferUnavailableException;
import com.personalloan.applicationmanagement.domain.invitation.OfferDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfferManagementAdapterTest {

    private WireMockServer wireMock;
    private OfferManagementAdapter adapter;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .requestFactory(factory)
                .build();

        adapter = new OfferManagementAdapter(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void validateInvitation_success_doesNotThrow() {
        wireMock.stubFor(get(urlEqualTo("/invitations/inv-001/validate"))
                .willReturn(aResponse().withStatus(200)));

        adapter.validateInvitation("inv-001");

        wireMock.verify(getRequestedFor(urlEqualTo("/invitations/inv-001/validate")));
    }

    @Test
    void validateInvitation_notFound_throwsInvitationNotFoundException() {
        wireMock.stubFor(get(urlEqualTo("/invitations/inv-404/validate"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> adapter.validateInvitation("inv-404"))
                .isInstanceOf(InvitationNotFoundException.class);
    }

    @Test
    void validateInvitation_expired_throwsInvitationExpiredException() {
        wireMock.stubFor(get(urlEqualTo("/invitations/inv-expired/validate"))
                .willReturn(aResponse().withStatus(410)));

        assertThatThrownBy(() -> adapter.validateInvitation("inv-expired"))
                .isInstanceOf(InvitationExpiredException.class);
    }

    @Test
    void validateInvitation_serverError_throwsOfferUnavailableException() {
        wireMock.stubFor(get(urlEqualTo("/invitations/inv-001/validate"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.validateInvitation("inv-001"))
                .isInstanceOf(OfferUnavailableException.class);
    }

    @Test
    void retrieveOffer_success_returnsOfferDetails() {
        wireMock.stubFor(get(urlEqualTo("/invitations/inv-001/offer"))
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

        OfferDetails offer = adapter.retrieveOffer("inv-001");

        assertThat(offer.offerId()).isEqualTo("offer-001");
        assertThat(offer.customerReferenceId()).isEqualTo("cref-001");
        assertThat(offer.termMonths()).isEqualTo(36);
    }
}
