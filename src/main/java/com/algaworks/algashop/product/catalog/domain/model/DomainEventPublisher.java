package com.algaworks.algashop.product.catalog.domain.model;

public interface DomainEventPublisher {
    void publisher(Object event);
}
