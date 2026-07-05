package com.personalloan.applicationmanagement.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseAcceptanceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    protected static WireMockServer offerManagementMock;
    protected static WireMockServer customerProfileMock;
    protected static WireMockServer ssnVerificationMock;
    protected static WireMockServer boltMock;

    static {
        offerManagementMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        customerProfileMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        ssnVerificationMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        boltMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        offerManagementMock.start();
        customerProfileMock.start();
        ssnVerificationMock.start();
        boltMock.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("integration.bolt.base-url",
                () -> "http://localhost:" + boltMock.port());
        registry.add("integration.bolt.api-key", () -> "test-bolt-key");
        registry.add("integration.offer-management.base-url",
                () -> "http://localhost:" + offerManagementMock.port());
        registry.add("integration.customer-profile.base-url",
                () -> "http://localhost:" + customerProfileMock.port());
        registry.add("integration.ssn-verification.base-url",
                () -> "http://localhost:" + ssnVerificationMock.port());
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @BeforeEach
    void resetMocks() {
        offerManagementMock.resetAll();
        customerProfileMock.resetAll();
        ssnVerificationMock.resetAll();
        boltMock.resetAll();
    }

    @AfterEach
    void afterEach() {
        // intentionally empty — containers are shared
    }

    protected String baseUrl(String path) {
        return "http://localhost:" + port + "/api/v1/application-management" + path;
    }
}
