package com.algaworks.algashop.product.catalog.application.product.event;

import com.algaworks.algashop.product.catalog.application.IntegrationEvent;

public interface ProductIntegrationEventPublisher {
	void send(IntegrationEvent event);
}