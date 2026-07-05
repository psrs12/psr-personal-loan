package com.personalloan.pricingorchestration.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        // The JDK HttpClient defaults to preferring HTTP/2, which some plain-HTTP servers
        // (e.g. WireMock/Jetty in test/stub environments) don't negotiate cleanly, causing
        // "RST_STREAM: Stream cancelled" I/O errors. Force HTTP/1.1 for all outbound calls.
        HttpClient jdkHttpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(jdkHttpClient));
    }
}
