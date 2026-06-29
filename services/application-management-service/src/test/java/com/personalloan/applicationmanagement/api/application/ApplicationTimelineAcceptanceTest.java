package com.personalloan.applicationmanagement.api.application;

import com.personalloan.applicationmanagement.api.BaseAcceptanceTest;
import com.personalloan.applicationmanagement.application.application.SSNTokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTimelineAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private SSNTokenStore ssnTokenStore;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer test-token");
        h.set("X-Channel-ID", "WEB");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void getTimeline_directApplication_returnsChronologicalEvents() {
        boltMock.stubFor(post(urlEqualTo("/tokenize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\": \"bolt-tok-test-123\"}")));

        String token = "timeline-token-" + System.currentTimeMillis();
        ssnTokenStore.store(token, java.time.LocalDateTime.now().plusMinutes(10));

        Map<String, Object> body = Map.of(
                "ssnVerificationToken", token,
                "ssn", "123456789",
                "firstName", "John",
                "lastName", "Smith",
                "dateOfBirth", "1990-01-15",
                "citizenship", "US_CITIZEN",
                "email", "john@example.com",
                "phone", "555-1234",
                "street", "123 Main St",
                "city", "New York",
                "state", "NY",
                "zip", "10001",
                "annualIncome", 80000.00,
                "requestedAmount", 10000.00,
                "termMonths", 36
        );

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                baseUrl("/applications"),
                new HttpEntity<>(body, headers()),
                Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String applicationId = (String) createResponse.getBody().get("applicationId");

        ResponseEntity<Map> timelineResponse = restTemplate.exchange(
                baseUrl("/applications/" + applicationId + "/timeline"),
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                Map.class);

        assertThat(timelineResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(timelineResponse.getBody().get("applicationId")).isEqualTo(applicationId);

        List<Map<String, Object>> events = (List<Map<String, Object>>) timelineResponse.getBody().get("events");
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).get("eventType")).isEqualTo("APPLICATION_CREATED");
    }

    @Test
    void getTimeline_unknownApplicationId_returns404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl("/applications/" + UUID.randomUUID() + "/timeline"),
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTimeline_missingAuthorizationHeader_returns401() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Channel-ID", "WEB");

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl("/applications/" + UUID.randomUUID() + "/timeline"),
                HttpMethod.GET,
                new HttpEntity<>(h),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
