package com.personalloan.applicationmanagement.infrastructure.messaging;

import com.personalloan.applicationmanagement.domain.application.ApplicationCreatedEvent;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class KafkaApplicationEventPublisher implements ApplicationEventPublisher {

    private final KafkaTemplate<String, ApplicationCreatedEvent> kafkaTemplate;
    private final String topic;

    public KafkaApplicationEventPublisher(KafkaTemplate<String, ApplicationCreatedEvent> kafkaTemplate,
                                          @Value("${kafka.topics.application-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Defers the actual Kafka send until the enclosing JPA transaction has committed. Without
     * this, a fast consumer (pricing-orchestration-service) can receive the event and call back
     * into this service's internal status endpoint before the INSERT is durably visible,
     * producing a spurious "Application not found" 404 on the consumer's first delivery attempt.
     * If no transaction is active (e.g. called from a non-transactional context), send immediately.
     */
    @Override
    public void publishApplicationCreated(ApplicationCreatedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaTemplate.send(topic, event.applicationId().toString(), event);
                }
            });
        } else {
            kafkaTemplate.send(topic, event.applicationId().toString(), event);
        }
    }
}
