package com.personalloan.applicationmanagement.infrastructure.messaging;

import com.personalloan.applicationmanagement.domain.application.ApplicationCreatedEvent;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaApplicationEventPublisher implements ApplicationEventPublisher {

    private final KafkaTemplate<String, ApplicationCreatedEvent> kafkaTemplate;
    private final String topic;

    public KafkaApplicationEventPublisher(KafkaTemplate<String, ApplicationCreatedEvent> kafkaTemplate,
                                          @Value("${kafka.topics.application-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishApplicationCreated(ApplicationCreatedEvent event) {
        kafkaTemplate.send(topic, event.applicationId().toString(), event);
    }
}
