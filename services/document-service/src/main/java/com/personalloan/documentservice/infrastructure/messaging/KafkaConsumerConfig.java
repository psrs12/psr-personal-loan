package com.personalloan.documentservice.infrastructure.messaging;

import com.personalloan.documentservice.domain.document.event.FinalDecisionDocumentsRequiredEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, FinalDecisionDocumentsRequiredEvent> documentsRequiredConsumerFactory() {
        JsonDeserializer<FinalDecisionDocumentsRequiredEvent> deserializer =
                new JsonDeserializer<>(FinalDecisionDocumentsRequiredEvent.class, false);

        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(null), null, deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FinalDecisionDocumentsRequiredEvent>
    documentsRequiredListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FinalDecisionDocumentsRequiredEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(documentsRequiredConsumerFactory());
        return factory;
    }
}
