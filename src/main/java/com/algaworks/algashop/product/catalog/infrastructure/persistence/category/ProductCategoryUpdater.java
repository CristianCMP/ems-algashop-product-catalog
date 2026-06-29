package com.algaworks.algashop.product.catalog.infrastructure.persistence.category;

import com.algaworks.algashop.product.catalog.application.category.event.CategoryUpdatedEvent;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductCategoryUpdater {

    private final MongoOperations mongoOperations;

    public void copyCategoryDataToProducts(CategoryUpdatedEvent event) {
        Query query = new Query(
                Criteria.where("categoryId").is(event.getCategoryId())
        );

        Update update = new Update()
                .set("category.name", event.getName())
                .set("category.enabled", event.getEnabled());

        mongoOperations.updateMulti(query, update, Product.class);
    }
}
