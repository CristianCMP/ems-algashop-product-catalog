package com.algaworks.algashop.product.catalog.infrastructure.kafka;

import com.algaworks.algashop.product.catalog.application.product.event.ProductIntegrationEventPublisher;
import com.algaworks.algashop.product.catalog.infrastructure.utility.BeanValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
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
    public ProductIntegrationEventPublisher productIntegrationEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            AlgaShopMessagingKafkaProperties properties,
            BeanValidationUtil beanValidationUtil) {
        return event -> {
            beanValidationUtil.validate(event);
            SendResult<String, Object> result = null;
            try {
                result = kafkaTemplate.send(properties.getProductEventTopicName(), event.getAggregateId(), event)
                        .get(40, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (TimeoutException | ExecutionException | KafkaException e) {
                throw new RuntimeException(e);
            }

            RecordMetadata metadata = result.getRecordMetadata();

            log.info("Published {} to {}-{} at offset {}",
                    event.getClass().getSimpleName(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset()
            );
        };
    }
}