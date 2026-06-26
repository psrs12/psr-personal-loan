package com.personalloan.applicationmanagement.api.ssn;

import com.personalloan.applicationmanagement.api.BaseAcceptanceTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class SSNAcceptanceTest extends BaseAcceptanceTest {

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer test-token");
        h.set("X-Channel-ID", "WEB");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private void stubBoltTokenization() {
        boltMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\": \"bolt-tok-test-999\"}")));
    }

    @Test
    void verifySSN_success_returnsVerificationToken() {
        stubBoltTokenization();
        ssnVerificationMock.stubFor(post(urlEqualTo("/ssn/verify"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "verificationToken": "tok-abc-123",
                                  "expiresAt": "2027-01-01T10:00:00"
                                }
                                """)));

        var body = Map.of("ssn", "123456789");
        var request = new HttpEntity<>(body, headers());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/ssn/verify"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("verificationToken")).isEqualTo("tok-abc-123");
        boltMock.verify(postRequestedFor(urlEqualTo("/tokenize")));
    }

    @Test
    void verifySSN_verificationFails_returns422() {
        stubBoltTokenization();
        ssnVerificationMock.stubFor(post(urlEqualTo("/ssn/verify"))
                .willReturn(aResponse().withStatus(422)));

        var body = Map.of("ssn", "000000000");
        var request = new HttpEntity<>(body, headers());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/ssn/verify"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("errorCode")).isEqualTo("SSN_VERIFICATION_FAILED");
    }
}
