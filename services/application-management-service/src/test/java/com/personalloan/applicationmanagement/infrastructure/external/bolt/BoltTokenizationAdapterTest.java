package com.personalloan.applicationmanagement.infrastructure.external.bolt;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.personalloan.applicationmanagement.domain.exception.TokenizationUnavailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoltTokenizationAdapterTest {

    private WireMockServer wireMock;
    private BoltTokenizationAdapter adapter;

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

        adapter = new BoltTokenizationAdapter(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void tokenize_success_returnsToken() {
        wireMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\": \"bolt-tok-abc123\"}")));

        String result = adapter.tokenize("123456789");

        assertThat(result).isEqualTo("bolt-tok-abc123");
        wireMock.verify(postRequestedFor(urlEqualTo("/tokenize")));
    }

    @Test
    void tokenize_serverError_throwsTokenizationUnavailableException() {
        wireMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.tokenize("123456789"))
                .isInstanceOf(TokenizationUnavailableException.class);
    }

    @Test
    void tokenize_clientError_throwsTokenizationUnavailableException() {
        wireMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse().withStatus(400)));

        assertThatThrownBy(() -> adapter.tokenize("123456789"))
                .isInstanceOf(TokenizationUnavailableException.class);
    }

    @Test
    void tokenize_emptyResponse_throwsTokenizationUnavailableException() {
        wireMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\": null}")));

        assertThatThrownBy(() -> adapter.tokenize("123456789"))
                .isInstanceOf(TokenizationUnavailableException.class);
    }
}
