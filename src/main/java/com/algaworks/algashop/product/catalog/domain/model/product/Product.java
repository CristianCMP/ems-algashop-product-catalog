package com.algaworks.algashop.product.catalog.domain.model.product;

import com.algaworks.algashop.product.catalog.domain.model.DomainException;
import com.algaworks.algashop.product.catalog.domain.model.IdGenerator;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Document(collection = "products")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@CompoundIndexes({
        /*
            There are two ways to do this.
            One is to use the 'pidx' prefix, in which case you don't need to use 'category._id', only 'category.id'.
            Otherwise, if you use the 'idx' prefix, you need to use 'category._id'.
        */
        @CompoundIndex(name = "pidx_product_by_category_enabled_salePrice", def = "{'category.id' : 1, 'enabled': 1, 'salePrice': 1}"),
        @CompoundIndex(name = "idx_product_by_category_enabled_addedAt", def = "{'category._id' : 1, 'enabled': 1, 'addedAt': -1}")
})
public class Product {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @TextIndexed(weight = 1)
    private String name;

    @Indexed(name = "idx_product_by_brand")
    private String brand;

    @TextIndexed(weight = 5)
    private String description;

    private Integer quantityInStock = 0;

    private Boolean enabled;

    private BigDecimal regularPrice;

    private BigDecimal salePrice;

    @Version
    private Long version;

    @CreatedDate
    private OffsetDateTime addedAt;

    @LastModifiedDate
    private OffsetDateTime updatedAt;

    @CreatedBy
    private UUID createdByUserId;

    @LastModifiedBy
    private UUID lastModifiedByUserId;

//    @DBRef
//    @Indexed(name = "idx_product_by_category")
//    @DocumentReference
//    @Field(name = "categoryId")
//    private Category category;
//    private UUID categoryId;

    private ProductCategory category;

    private Integer discountPercentageRounded;

    @TextScore
    private Float score;

    @Builder
    public Product(String name, String brand, String description,
                   Boolean enabled, BigDecimal regularPrice, BigDecimal salePrice, Category category) {
        this.setId(IdGenerator.generateTimeBasedUUID());
        this.setName(name);
        this.setBrand(brand);
        this.setDescription(description);
        this.setEnabled(enabled);
        this.setRegularPrice(regularPrice);
        this.setSalePrice(salePrice);
        this.setCategory(category);
    }

    public void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    public void setBrand(String brand) {
        if (StringUtils.isBlank(brand)) {
            throw new IllegalArgumentException();
        }
        this.brand = brand;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRegularPrice(BigDecimal regularPrice) {
        Objects.requireNonNull(regularPrice);
        if (regularPrice.signum() == -1) {
            throw new IllegalArgumentException();
        }

        if (this.salePrice == null) {
            this.salePrice = regularPrice;
        } else if(regularPrice.compareTo(this.salePrice) < 0) {
            throw new DomainException("Sale price cannot be greater than regular price");
        }
        this.regularPrice = regularPrice;
        this.calculateDiscountPercentage();
    }

    public void setSalePrice(BigDecimal salePrice) {
        Objects.requireNonNull(salePrice);
        if (salePrice.signum() == -1) {
            throw new IllegalArgumentException();
        }

        if (this.regularPrice == null) {
            this.regularPrice = salePrice;
        } else if (this.regularPrice.compareTo(salePrice) < 0) {
            throw new DomainException("Sale price cannot be greater than regular price");
        }
        this.salePrice = salePrice;
        this.calculateDiscountPercentage();
    }

    public void setEnabled(Boolean enabled) {
        Objects.requireNonNull(enabled);
        this.enabled = enabled;
    }

    public void setCategory(Category category) {
        Objects.requireNonNull(category);
        this.category = ProductCategory.of(category);
//        #Version 2
//        this.categoryId = category.getId();
//        #Version 1
//        this.category = category;
    }

    public void disable() {
        this.setEnabled(false);
    }

    public void enable() {
        this.setEnabled(true);
    }

    public boolean isInStock() {
        return this.getQuantityInStock() != null && this.getQuantityInStock() > 0;
    }

    public boolean getHasDiscount() {
        return getDiscountPercentageRounded() != null && getDiscountPercentageRounded() > 0;
    }

    private void setId(UUID id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    private void setQuantityInStock(Integer quantityInStock) {
        Objects.requireNonNull(quantityInStock);
        if (quantityInStock < 0) {
            throw new IllegalArgumentException();
        }
        this.quantityInStock =quantityInStock;
    }

    private void calculateDiscountPercentage() {
        if (regularPrice == null || salePrice == null || regularPrice.signum() == 0) {
            discountPercentageRounded = 0;
            return;
        }

        discountPercentageRounded = BigDecimal.ONE
                .subtract(salePrice.divide(regularPrice, 4, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }
}