package com.personalloan.applicationmanagement.api.application;

import com.personalloan.applicationmanagement.api.BaseAcceptanceTest;
import com.personalloan.applicationmanagement.application.application.SSNTokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private SSNTokenStore ssnTokenStore;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer test-token");
        h.set("X-Channel-ID", "WEB");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private Map<String, Object> directApplicationBody(String token) {
        return Map.of(
                "ssnVerificationToken", token,
                "ssn", "123456789",
                "firstName", "Jane",
                "lastName", "Doe",
                "dateOfBirth", "1985-06-15",
                "citizenship", "US_CITIZEN",
                "email", "jane@example.com",
                "phone", "555-5678",
                "street", "456 Oak Ave",
                "city", "Chicago",
                "state", "IL",
                "zip", "60601",
                "annualIncome", 90000.00,
                "requestedAmount", 15000.00,
                "termMonths", 48
        );
    }

    private void stubBoltTokenization() {
        boltMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\": \"bolt-tok-test-123\"}")));
    }

    @Test
    void createApplication_directPath_returnsApplicationId_andSsnTokenStored() {
        stubBoltTokenization();
        String token = "direct-token-" + System.currentTimeMillis();
        ssnTokenStore.store(token, java.time.LocalDateTime.now().plusMinutes(10));

        var request = new HttpEntity<>(directApplicationBody(token), headers());
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/applications"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("applicationId");
        assertThat(response.getBody().get("applicationSource")).isEqualTo("DIRECT");
        boltMock.verify(postRequestedFor(urlEqualTo("/tokenize")));
    }

    @Test
    void createApplication_withoutSsnToken_returns400() {
        Map<String, Object> body = Map.of(
                "ssnVerificationToken", "invalid-token-xyz",
                "ssn", "123456789",
                "firstName", "Jane",
                "lastName", "Doe",
                "dateOfBirth", "1985-06-15",
                "citizenship", "US_CITIZEN",
                "email", "jane@example.com",
                "phone", "555-5678",
                "street", "456 Oak Ave",
                "city", "Chicago",
                "state", "IL",
                "zip", "60601",
                "annualIncome", 90000.00,
                "requestedAmount", 15000.00,
                "termMonths", 48
        );

        var request = new HttpEntity<>(body, headers());
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/applications"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("errorCode")).isEqualTo("SSN_VERIFICATION_TOKEN_INVALID");
    }

    @Test
    void createApplication_missingAuthorizationHeader_returns401() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Channel-ID", "WEB");
        h.setContentType(MediaType.APPLICATION_JSON);

        var request = new HttpEntity<>(directApplicationBody("any-token"), h);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/applications"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
