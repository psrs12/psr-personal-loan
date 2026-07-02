package com.personalloan.pricingorchestration.infrastructure.messaging;

import com.personalloan.pricingorchestration.domain.pricing.event.ApplicationCreatedEvent;
import com.personalloan.pricingorchestration.domain.pricing.event.ConsentCapturedEvent;
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
    public ConsumerFactory<String, ApplicationCreatedEvent> applicationCreatedConsumerFactory() {
        JsonDeserializer<ApplicationCreatedEvent> deserializer =
                new JsonDeserializer<>(ApplicationCreatedEvent.class, false);
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(null), null, deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent>
    applicationCreatedListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(applicationCreatedConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ConsentCapturedEvent> consentCapturedConsumerFactory() {
        JsonDeserializer<ConsentCapturedEvent> deserializer =
                new JsonDeserializer<>(ConsentCapturedEvent.class, false);
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(null), null, deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ConsentCapturedEvent>
    consentCapturedListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ConsentCapturedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consentCapturedConsumerFactory());
        return factory;
    }
}
