package com.algaworks.algashop.product.catalog.application.category.query;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CategoryOutputTestDataBuilder {

    private CategoryOutputTestDataBuilder() {
    }

    public static CategoryDetailOutput.CategoryDetailOutputBuilder aCategory() {
        return CategoryDetailOutput.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .enabled(true)
                .updatedAt(OffsetDateTime.now());
    }

    public static CategoryDetailOutput.CategoryDetailOutputBuilder aDisabledCategory() {
        return CategoryDetailOutput.builder()
                .id(UUID.randomUUID())
                .name("Books")
                .enabled(false)
                .updatedAt(OffsetDateTime.now());
    }
}