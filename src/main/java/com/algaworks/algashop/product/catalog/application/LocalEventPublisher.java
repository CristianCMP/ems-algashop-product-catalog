package com.algaworks.algashop.product.catalog.application;

public interface LocalEventPublisher {
    void send(Object message);
}
