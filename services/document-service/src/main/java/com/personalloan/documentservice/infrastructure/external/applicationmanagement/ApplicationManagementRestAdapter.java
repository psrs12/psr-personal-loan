package com.personalloan.documentservice.infrastructure.external.applicationmanagement;

import com.personalloan.documentservice.domain.document.port.ApplicationManagementPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class ApplicationManagementRestAdapter implements ApplicationManagementPort {

    private static final Logger log = LoggerFactory.getLogger(ApplicationManagementRestAdapter.class);

    private final RestClient restClient;

    public ApplicationManagementRestAdapter(
            @Value("${services.application-management.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void updateApplicationStatus(UUID applicationId, String status) {
        log.info("Updating application {} to status {}", applicationId, status);
        restClient.patch()
                .uri("/applications/{id}/status", applicationId)
                .body(Map.of("status", status))
                .retrieve()
                .toBodilessEntity();
    }
}
