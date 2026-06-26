package com.personalloan.applicationmanagement.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient offerManagementRestClient(
            @Value("${integration.offer-management.base-url}") String baseUrl,
            @Value("${integration.offer-management.timeout-seconds}") int timeoutSeconds) {
        return buildRestClient(baseUrl, timeoutSeconds);
    }

    @Bean
    public RestClient customerProfileRestClient(
            @Value("${integration.customer-profile.base-url}") String baseUrl,
            @Value("${integration.customer-profile.timeout-seconds}") int timeoutSeconds) {
        return buildRestClient(baseUrl, timeoutSeconds);
    }

    @Bean
    public RestClient boltRestClient(
            @Value("${integration.bolt.base-url}") String baseUrl,
            @Value("${integration.bolt.timeout-seconds}") int timeoutSeconds) {
        return buildRestClient(baseUrl, timeoutSeconds);
    }

    @Bean
    public RestClient ssnVerificationRestClient(
            @Value("${integration.ssn-verification.base-url}") String baseUrl,
            @Value("${integration.ssn-verification.timeout-seconds}") int timeoutSeconds) {
        return buildRestClient(baseUrl, timeoutSeconds);
    }

    private RestClient buildRestClient(String baseUrl, int timeoutSeconds) {
        int millis = (int) Duration.ofSeconds(timeoutSeconds).toMillis();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(millis);
        factory.setReadTimeout(millis);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
