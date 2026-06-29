package com.personalloan.applicationmanagement.infrastructure.external.customer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.personalloan.applicationmanagement.domain.invitation.CustomerPrefill;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class CustomerProfileAdapterTest {

    private WireMockServer wireMock;
    private CustomerProfileAdapter adapter;

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

        adapter = new CustomerProfileAdapter(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void retrieveCustomer_found_returnsPrefill() {
        wireMock.stubFor(get(urlEqualTo("/customers/cref-001"))
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

        Optional<CustomerPrefill> result = adapter.retrieveCustomer("cref-001");

        assertThat(result).isPresent();
        assertThat(result.get().firstName()).isEqualTo("John");
        assertThat(result.get().lastName()).isEqualTo("Doe");
        assertThat(result.get().state()).isEqualTo("IL");
    }

    @Test
    void retrieveCustomer_notFound_returnsEmpty() {
        wireMock.stubFor(get(urlEqualTo("/customers/cref-999"))
                .willReturn(aResponse().withStatus(404)));

        Optional<CustomerPrefill> result = adapter.retrieveCustomer("cref-999");

        assertThat(result).isEmpty();
    }

    @Test
    void retrieveCustomer_serverError_returnsEmpty() {
        wireMock.stubFor(get(urlEqualTo("/customers/cref-001"))
                .willReturn(aResponse().withStatus(503)));

        Optional<CustomerPrefill> result = adapter.retrieveCustomer("cref-001");

        assertThat(result).isEmpty();
    }
}
