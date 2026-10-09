package com.algaworks.algashop.product.catalog.application.product.event;

import com.algaworks.algashop.product.catalog.application.IntegrationEvent;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private UUID productId;
    @NotNull
    private OffsetDateTime changeAt;
    @NotNull
    private BigDecimal oldRegularPrice;
    @NotNull
    private BigDecimal oldSalePrice;
    @NotNull
    private BigDecimal newRegularPrice;
    @NotNull
    private BigDecimal newSalePrice;

    @Override
    public String getAggregateId() {
        return productId.toString();
    }
}
