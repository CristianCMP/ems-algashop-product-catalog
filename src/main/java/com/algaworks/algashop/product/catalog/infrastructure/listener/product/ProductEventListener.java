package com.algaworks.algashop.product.catalog.infrastructure.listener.product;

import com.algaworks.algashop.product.catalog.application.IntegrationEventPublisher;
import com.algaworks.algashop.product.catalog.domain.model.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductEventListener {

    private final IntegrationEventPublisher integrationEventPublisher;

    @EventListener(ProductPriceChangedEvent.class)
    public void handle(ProductPriceChangedEvent event) {
        log.info("ProductPriceChangedEvent: {}", event);
        integrationEventPublisher.send(event, "product-catalog.products.events");
    }

    @EventListener(ProductPlacedOnSaleEvent.class)
    public void handle(ProductPlacedOnSaleEvent event) {
        log.info("ProductPlacedOnSaleEvent: {}", event);
        integrationEventPublisher.send(event, "product-catalog.products.events");
    }

    @EventListener(ProductAddedEvent.class)
    public void handle(ProductAddedEvent event) {
        log.info("ProductAddedEvent: {}", event);
        integrationEventPublisher.send(event, "product-catalog.products.events");
    }

    @EventListener(ProductDelistedEvent.class)
    public void handle(ProductDelistedEvent event) {
        log.info("ProductDelistedEvent: {}", event);
        integrationEventPublisher.send(event, "product-catalog.products.events");
    }

    @EventListener(ProductListedEvent.class)
    public void handle(ProductListedEvent event) {
        log.info("ProductListedEvent: {}", event);
        integrationEventPublisher.send(event, "product-catalog.products.events");
    }

    @EventListener(ProductRestockedEvent.class)
    public void handle(ProductRestockedEvent event) {
        log.info("ProductRestockedEvent: {}", event);
        integrationEventPublisher.send(event, "product-catalog.products.events");
    }

    @EventListener(ProductSoldOutEvent.class)
    public void handle(ProductSoldOutEvent event) {
        log.info("ProductSoldOutEvent: {}", event);
        integrationEventPublisher.send(event, "product-catalog.products.events");
    }

}
