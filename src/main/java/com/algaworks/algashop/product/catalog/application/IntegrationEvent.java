package com.algaworks.algashop.product.catalog.application;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public interface IntegrationEvent {
	@JsonIgnore
	String getAggregateId();
	@JsonIgnore
	UUID getIdempotencyKey();
}