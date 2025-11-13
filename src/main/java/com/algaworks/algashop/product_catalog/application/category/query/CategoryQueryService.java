package com.algaworks.algashop.product_catalog.application.category.query;

import com.algaworks.algashop.product_catalog.application.PageModel;

import java.util.UUID;

public interface CategoryQueryService {
    PageModel<CategoryDetailOutput> filter(Integer size, Integer number);
    CategoryDetailOutput findById(UUID categoryId);
}