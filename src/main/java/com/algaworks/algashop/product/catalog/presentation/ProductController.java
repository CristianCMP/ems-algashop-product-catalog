package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.application.PageModel;
import com.algaworks.algashop.product.catalog.application.product.management.ProductInput;
import com.algaworks.algashop.product.catalog.application.product.management.ProductManagementApplicationService;
import com.algaworks.algashop.product.catalog.application.product.query.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.query.ProductFilter;
import com.algaworks.algashop.product.catalog.application.product.query.ProductQueryService;
import com.algaworks.algashop.product.catalog.application.product.query.ProductSummaryOutput;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

import static com.algaworks.algashop.product.catalog.infrastructure.security.SecurityAnnotations.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductManagementApplicationService productManagementApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CanWriteProducts
    public ProductDetailOutput create(@RequestBody @Valid ProductInput input) {
        try {
            return productManagementApplicationService.create(input);
        } catch (CategoryNotFoundException e) {
            throw new UnprocessableContentException(e.getMessage(), e);
        }
    }

    @GetMapping("/{productId}")
    @CanReadProducts
    public ResponseEntity<ProductDetailOutput> findById(@PathVariable UUID productId) {
        ProductDetailOutput product = productQueryService.findById(productId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
                .eTag("product:id:" + product.getId() + ":v:" + product.getVersion())
                .lastModified(product.getUpdatedAt().toInstant())
                .body(product);
    }

    @PutMapping("/{productId}")
    @CanWriteProducts
    public ProductDetailOutput update(@PathVariable UUID productId,
                                      @RequestBody @Valid ProductInput input) {
        return productManagementApplicationService.update(productId, input);
    }

    @DeleteMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CanWriteProducts
    public void disable(@PathVariable UUID productId) {
        productManagementApplicationService.disable(productId);
    }

    @PutMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CanWriteProducts
    public void enable(@PathVariable UUID productId) {
        productManagementApplicationService.enable(productId);
    }

    @GetMapping
    @CanReadProducts
    public PageModel<ProductSummaryOutput> filter(ProductFilter productFilter) {
        return productQueryService.filter(productFilter);
    }

    @PostMapping("/{productId}/restock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CanWriteProductsStock
    public void restock(@PathVariable UUID productId, @RequestBody @Valid ProductQuantityModel productQuantityModel) {
        productManagementApplicationService.restock(productId, productQuantityModel.getQuantity());
    }

    @PostMapping("/{productId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CanWriteProductsStock
    public void withdraw(@PathVariable UUID productId, @RequestBody @Valid ProductQuantityModel productQuantityModel) {
        productManagementApplicationService.withdraw(productId, productQuantityModel.getQuantity());
    }

}