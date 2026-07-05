package com.personalloan.applicationmanagement.infrastructure.messaging;

import com.personalloan.applicationmanagement.domain.application.event.DocumentsCompletedEvent;
import com.personalloan.applicationmanagement.domain.application.event.ESignCompletedEvent;
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
    public ConsumerFactory<String, ESignCompletedEvent> esignCompletedConsumerFactory() {
        JsonDeserializer<ESignCompletedEvent> deserializer =
                new JsonDeserializer<>(ESignCompletedEvent.class, false);
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(null), null, deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ESignCompletedEvent>
    esignCompletedListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ESignCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(esignCompletedConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DocumentsCompletedEvent> documentsCompletedConsumerFactory() {
        JsonDeserializer<DocumentsCompletedEvent> deserializer =
                new JsonDeserializer<>(DocumentsCompletedEvent.class, false);
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(null), null, deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DocumentsCompletedEvent>
    documentsCompletedListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DocumentsCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(documentsCompletedConsumerFactory());
        return factory;
    }
}
