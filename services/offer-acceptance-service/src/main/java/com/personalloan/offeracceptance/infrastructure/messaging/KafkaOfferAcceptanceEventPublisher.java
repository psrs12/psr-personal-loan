package com.personalloan.offeracceptance.infrastructure.messaging;

import com.personalloan.offeracceptance.domain.port.OfferAcceptanceEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaOfferAcceptanceEventPublisher implements OfferAcceptanceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOfferAcceptanceEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaOfferAcceptanceEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                               @Value("${kafka.topics.offer-acceptance-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishESignCompleted(UUID applicationId, UUID eSignId, LocalDateTime signedAt) {
        Map<String, Object> event = Map.of(
                "eventType", "ESignCompleted",
                "applicationId", applicationId.toString(),
                "eSignId", eSignId.toString(),
                "signedAt", signedAt.toString()
        );
        kafkaTemplate.send(topic, applicationId.toString(), event);
        log.info("Published ESignCompleted for application {}", applicationId);
    }
}
