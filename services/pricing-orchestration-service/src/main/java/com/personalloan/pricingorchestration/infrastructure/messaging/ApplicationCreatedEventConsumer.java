package com.personalloan.pricingorchestration.infrastructure.messaging;

import com.personalloan.pricingorchestration.application.pricing.SoftPullOrchestrationService;
import com.personalloan.pricingorchestration.domain.pricing.event.ApplicationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationCreatedEventConsumer.class);

    private final SoftPullOrchestrationService softPullOrchestrationService;

    public ApplicationCreatedEventConsumer(SoftPullOrchestrationService softPullOrchestrationService) {
        this.softPullOrchestrationService = softPullOrchestrationService;
    }

    @KafkaListener(topics = "${kafka.topics.application-events}", groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "applicationCreatedListenerContainerFactory")
    public void onApplicationCreated(ApplicationCreatedEvent event) {
        log.info("Received ApplicationCreated event for application {}", event.applicationId());
        softPullOrchestrationService.initiateSoftPull(event.applicationId(), null);
    }
}
