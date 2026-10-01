package com.algaworks.algashop.product.catalog.application.product.event;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAddedIntegrationEvent {
    private UUID productId;
    private OffsetDateTime addedAt;
}
