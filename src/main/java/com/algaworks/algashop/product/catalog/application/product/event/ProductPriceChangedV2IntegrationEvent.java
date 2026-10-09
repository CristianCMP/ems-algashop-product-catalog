package com.algaworks.algashop.product.catalog.application.product.event;

import com.algaworks.algashop.product.catalog.application.IntegrationEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceChangedV2IntegrationEvent implements IntegrationEvent {
    private UUID productId;
    private OffsetDateTime changeAt;
    private BigDecimal oldRegularPrice;
    private BigDecimal oldSalePrice;
    private BigDecimal newRegularPrice;
    private BigDecimal newSalePrice;

    @Override
    public String getAggregateId() {
        return productId.toString();
    }
}
