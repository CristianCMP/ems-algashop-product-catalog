package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.application.PageModel;
import com.algaworks.algashop.product.catalog.application.category.management.CategoryInput;
import com.algaworks.algashop.product.catalog.application.category.management.CategoryManagementApplicationService;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryDetailOutput;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryFilter;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.algaworks.algashop.product.catalog.infrastructure.security.SecurityAnnotations.CanReadCategories;
import static com.algaworks.algashop.product.catalog.infrastructure.security.SecurityAnnotations.CanWriteCategories;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;
    private final CategoryManagementApplicationService categoryManagementApplicationService;

    @GetMapping
    @CanReadCategories
    public ResponseEntity<PageModel<CategoryDetailOutput>> filter(CategoryFilter filter,
                                                                  WebRequest webRequest) {
        if (!filter.isCacheable()) {
            PageModel<CategoryDetailOutput> result = categoryQueryService.filter(filter);
            return ResponseEntity.ok(result);
        }

        OffsetDateTime lastModified = categoryQueryService.lastModified();

        if (webRequest.checkNotModified(lastModified.toInstant().toEpochMilli())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        PageModel<CategoryDetailOutput> result = categoryQueryService.filter(filter);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .lastModified(lastModified.toInstant())
                .body(result);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CanWriteCategories
    public CategoryDetailOutput create(@RequestBody @Valid CategoryInput input) {
        UUID categoryId = categoryManagementApplicationService.create(input);
        return categoryQueryService.findById(categoryId);
    }

    @GetMapping("/{categoryId}")
    @CanReadCategories
    public ResponseEntity<CategoryDetailOutput> findById(@PathVariable UUID categoryId) {
        CategoryDetailOutput category = categoryQueryService.findById(categoryId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .eTag("category:id:" + category.getId() + ":v:" + category.getVersion())
                .lastModified(category.getUpdatedAt().toInstant())
                .body(category);
    }

    @PutMapping("/{categoryId}")
    @CanWriteCategories
    public CategoryDetailOutput update(
            @PathVariable UUID categoryId,
            @RequestBody @Valid CategoryInput input) {
        categoryManagementApplicationService.update(categoryId, input);
        return categoryQueryService.findById(categoryId);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CanWriteCategories
    public void disable(@PathVariable UUID categoryId) {
        categoryManagementApplicationService.disable(categoryId);
    }
}