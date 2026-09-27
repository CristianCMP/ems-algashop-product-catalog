package com.algaworks.algashop.product.catalog.infrastructure.kafka;

import com.algaworks.algashop.product.catalog.application.IntegrationEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic productsEventTopic() {
        return TopicBuilder.name("product-catalog.product.events")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    public IntegrationEventPublisher integrationEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new IntegrationEventPublisher() {
            @Override
            public void send(Object event, String key, String destination) {
                kafkaTemplate.send(destination, key, event);
            }
        };
    }
}