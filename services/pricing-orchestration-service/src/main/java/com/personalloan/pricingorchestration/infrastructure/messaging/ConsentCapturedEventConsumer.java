package com.personalloan.pricingorchestration.infrastructure.messaging;

import com.personalloan.pricingorchestration.application.pricing.HardPullOrchestrationService;
import com.personalloan.pricingorchestration.domain.pricing.event.ConsentCapturedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConsentCapturedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConsentCapturedEventConsumer.class);

    private final HardPullOrchestrationService hardPullOrchestrationService;

    public ConsentCapturedEventConsumer(HardPullOrchestrationService hardPullOrchestrationService) {
        this.hardPullOrchestrationService = hardPullOrchestrationService;
    }

    @KafkaListener(topics = "${kafka.topics.consent-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onConsentCaptured(ConsentCapturedEvent event) {
        log.info("Received ConsentCaptured event for application {}", event.applicationId());
        hardPullOrchestrationService.initiateHardPull(
                event.applicationId(),
                event.selectedPricingOfferId(),
                event.applicantReference());
    }
}
