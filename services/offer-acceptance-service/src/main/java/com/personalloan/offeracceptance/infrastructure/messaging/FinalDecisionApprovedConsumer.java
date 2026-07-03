package com.personalloan.offeracceptance.infrastructure.messaging;

import com.personalloan.offeracceptance.application.offer.CreateSessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class FinalDecisionApprovedConsumer {

    private static final Logger log = LoggerFactory.getLogger(FinalDecisionApprovedConsumer.class);

    private final CreateSessionUseCase useCase;

    public FinalDecisionApprovedConsumer(CreateSessionUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${kafka.topics.pricing-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onFinalDecisionApproved(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        if (!"FinalDecisionApproved".equals(eventType)) {
            return;
        }
        UUID applicationId = UUID.fromString((String) event.get("applicationId"));
        log.info("Received APPROVED decision for application {}", applicationId);
        useCase.execute(applicationId);
    }
}
