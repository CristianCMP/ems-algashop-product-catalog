package com.algaworks.algashop.product.catalog.application.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDelistedIntegrationEvent {
    private UUID productId;
    private OffsetDateTime delistedAt;
}