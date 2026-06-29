package com.personalloan.pricingorchestration.infrastructure.external;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.personalloan.pricingorchestration.infrastructure.external.creditmanagement.CreditManagementAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class CreditManagementAdapterTest {

    private WireMockServer wireMock;
    private CreditManagementAdapter adapter;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .requestFactory(factory)
                .build();

        adapter = new CreditManagementAdapter(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void initiateSoftPull_success_returnsCreditReportReferenceId() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/credit/soft-pull"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "creditReportReferenceId": "soft-ref-001",
                                  "status": "COMPLETED"
                                }
                                """)));

        String referenceId = adapter.initiateSoftPull(applicationId, "applicant-ref-001");

        assertThat(referenceId).isEqualTo("soft-ref-001");
        wireMock.verify(postRequestedFor(urlEqualTo("/credit/soft-pull")));
    }

    @Test
    void initiateSoftPull_serverError_throwsException() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/credit/soft-pull"))
                .willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> adapter.initiateSoftPull(applicationId, "applicant-ref-001"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void initiateHardPull_success_returnsCreditReportReferenceId() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/credit/hard-pull"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "creditReportReferenceId": "hard-ref-001",
                                  "status": "COMPLETED"
                                }
                                """)));

        String referenceId = adapter.initiateHardPull(applicationId, "applicant-ref-001", "soft-ref-001");

        assertThat(referenceId).isEqualTo("hard-ref-001");
        wireMock.verify(postRequestedFor(urlEqualTo("/credit/hard-pull")));
    }

    @Test
    void initiateHardPull_serverError_throwsException() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/credit/hard-pull"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.initiateHardPull(applicationId, "applicant-ref-001", "soft-ref-001"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void initiateHardPull_timeout_throwsException() {
        UUID applicationId = UUID.randomUUID();
        wireMock.stubFor(post(urlEqualTo("/credit/hard-pull"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(6000)
                        .withBody("{}")));

        assertThatThrownBy(() -> adapter.initiateHardPull(applicationId, "applicant-ref-001", "soft-ref-001"))
                .isInstanceOf(Exception.class);
    }
}
