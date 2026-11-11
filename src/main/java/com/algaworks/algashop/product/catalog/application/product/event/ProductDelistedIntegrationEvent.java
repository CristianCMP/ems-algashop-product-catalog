package com.algaworks.algashop.product.catalog.application.product.event;

import com.algaworks.algashop.product.catalog.application.IntegrationEvent;
import com.algaworks.algashop.product.catalog.domain.model.IdGenerator;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDelistedIntegrationEvent implements IntegrationEvent {
	private UUID idempotencyKey = IdGenerator.generateTimeBasedUUID();
	private UUID productId;
	private OffsetDateTime delistedAt;

	@Override
	public String getAggregateId() {
		if (productId == null) {
			return null;
		}
		return productId.toString();
	}
}