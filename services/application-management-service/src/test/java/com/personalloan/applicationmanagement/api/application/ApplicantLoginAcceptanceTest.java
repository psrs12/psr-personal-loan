package com.personalloan.applicationmanagement.api.application;

import com.personalloan.applicationmanagement.api.BaseAcceptanceTest;
import com.personalloan.applicationmanagement.application.application.SSNTokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicantLoginAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private SSNTokenStore ssnTokenStore;

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private Map<String, Object> createApplication() {
        boltMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\": \"tok-9991234\"}")));

        String token = "login-test-token-" + System.currentTimeMillis();
        ssnTokenStore.store(token, java.time.LocalDateTime.now().plusMinutes(10));

        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer test-token");
        h.set("X-Channel-ID", "WEB");
        h.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "ssnVerificationToken", token,
                "ssn", "000001234",
                "firstName", "Alice",
                "lastName", "Walker",
                "dateOfBirth", "1992-03-20",
                "citizenship", "US_CITIZEN",
                "email", "alice@example.com",
                "phone", "555-9999",
                "street", "1 Test St",
                "city", "Austin",
                "state", "TX",
                "zip", "78701",
                "annualIncome", 70000.00,
                "requestedAmount", 8000.00,
                "termMonths", 36
        );

        ResponseEntity<Map> resp = restTemplate.postForEntity(
                baseUrl("/applications"), new HttpEntity<>(body, h), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody();
    }

    @Test
    void login_validCredentials_returnsSessionToken() {
        Map<String, Object> app = createApplication();
        String applicationId = (String) app.get("applicationId");

        Map<String, Object> loginBody = Map.of(
                "applicationId", applicationId,
                "last4SSN", "1234",
                "dateOfBirth", "1992-03-20"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/applications/login"),
                new HttpEntity<>(loginBody, jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("sessionToken");
        assertThat(response.getBody()).containsKey("applicationStatus");
        assertThat(response.getBody().get("applicationId")).isEqualTo(applicationId);
    }

    @Test
    void login_wrongDob_returns401() {
        Map<String, Object> app = createApplication();
        String applicationId = (String) app.get("applicationId");

        Map<String, Object> loginBody = Map.of(
                "applicationId", applicationId,
                "last4SSN", "1234",
                "dateOfBirth", "1990-01-01"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/applications/login"),
                new HttpEntity<>(loginBody, jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("errorCode")).isEqualTo("APPLICANT_VERIFICATION_FAILED");
    }

    @Test
    void login_unknownApplicationId_returns404() {
        Map<String, Object> loginBody = Map.of(
                "applicationId", UUID.randomUUID().toString(),
                "last4SSN", "1234",
                "dateOfBirth", "1992-03-20"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl("/applications/login"),
                new HttpEntity<>(loginBody, jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
