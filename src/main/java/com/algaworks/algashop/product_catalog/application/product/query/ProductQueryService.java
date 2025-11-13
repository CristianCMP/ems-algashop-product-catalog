package com.algaworks.algashop.product_catalog.application.product.query;

import com.algaworks.algashop.product_catalog.application.PageModel;

import java.util.UUID;

public interface ProductQueryService {
    ProductDetailOutput findById(UUID productId);
    PageModel<ProductDetailOutput> filter(Integer size, Integer number);
}