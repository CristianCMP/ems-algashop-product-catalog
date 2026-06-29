package com.algaworks.algashop.product.catalog.infrastructure.persistence.product;

import com.algaworks.algashop.product.catalog.application.PageModel;
import com.algaworks.algashop.product.catalog.application.product.query.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.query.ProductFilter;
import com.algaworks.algashop.product.catalog.application.product.query.ProductQueryService;
import com.algaworks.algashop.product.catalog.application.product.query.ProductSummaryOutput;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;
    private final Mapper mapper;

    private final MongoOperations mongoOperations;

//    private static final String findWordRegex = "(?i)(?<= |^)%s(?= |$)"; //%s it's from java, used from complete words
    private static final String findWordRegex = "(?i)%s"; //%s it's from java, used from incomplete words

    @Override
    public ProductDetailOutput findById(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        return mapper.convert(product, ProductDetailOutput.class);
    }

//    Version 1
//    @Override
//    public PageModel<ProductSummaryOutput> filter(ProductFilter filter) {
//        Query query = queryWith(filter);
//        long totalItems = mongoOperations.count(query, Product.class);
//        Sort sort = sortWith(filter);
//
//        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), sort);
//        Query pagedQuery = query.with(pageRequest);
//
//        List<Product> products;
//        int totalPages =0;
//
//        if (totalItems > 0) {
//            products = mongoOperations.find(pagedQuery, Product.class);
//            totalPages = (int) Math.ceil((double) totalItems / pageRequest.getPageSize());
//        } else {
//            products = new ArrayList<>();
//        }
//
//        List<ProductSummaryOutput> productOutputs = products
//                .stream()
//                .map(p -> mapper.convert(p, ProductSummaryOutput.class))
//                .collect(Collectors.toList());
//
//        return PageModel.<ProductSummaryOutput>builder()
//                .content(productOutputs)
//                .number(pageRequest.getPageNumber())
//                .size(pageRequest.getPageSize())
//                .totalElements(totalItems)
//                .totalPages(totalPages)
//                .build();
//    }

    @Override
    public PageModel<ProductSummaryOutput> filter(ProductFilter filter) {
        Optional<Criteria> criteria = buildCriteria(filter);
        Optional<TextCriteria> textCriteria = buildTextCriteria(filter);

        List<AggregationOperation> operations = new ArrayList<>();

        textCriteria.ifPresent(c -> operations.add(match(c))); // text ever first
        criteria.ifPresent(c -> operations.add(match(c)));

        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize());

        operations.addAll(Arrays.asList(
                lookup("categories", "categoryId", "_id", "category"),
                unwind("$category"),
                sort(sortWith(filter)),
                projectForSummary(),
                skip(pageRequest.getOffset()),
                limit(filter.getSize())
        ));

        Aggregation aggregation = newAggregation(operations);

        List<ProductSummaryOutput> productSummaryOutputs = mongoOperations
                .aggregate(aggregation, Product.class, ProductSummaryOutput.class)
                .getMappedResults();

        return PageModel.<ProductSummaryOutput>builder()
                .content(productSummaryOutputs)
                .number(pageRequest.getPageNumber())
                .size(pageRequest.getPageSize())
                .totalElements(10)
                .totalPages(10)
                .build();
    }

    private ProjectionOperation projectForSummary() {
        return project()
                .and("_id").as("_id")
                .and("addedAt").as("addedAt")
                .and("name").as("name")
                .and("brand").as("brand")
                .and("regularPrice").as("regularPrice")
                .and("salePrice").as("salePrice")
                .and("enabled").as("enabled")
                .and("quantityInStock").as("quantityInStock")
                .and("discountPercentageRounded").as("discountPercentageRounded")
                .and("score").as("score")
                .and("category._id").as("category._id")
                .and("category.name").as("category.name");
    }

    private Optional<Criteria> buildCriteria(ProductFilter filter) {
        List<CriteriaDefinition> criterias = new ArrayList<>();

        if (filter.getEnabled() != null) {
            criterias.add(Criteria.where("enabled").is(filter.getEnabled()));
        }

        if (filter.getAddedAtFrom() != null && filter.getAddedAtTo() != null) {
            criterias.add(Criteria.where("addedAt")
                    .gte(filter.getAddedAtFrom())
                    .lte(filter.getAddedAtTo())
            );
        } else {
            if (filter.getAddedAtFrom() != null) {
                criterias.add(Criteria.where("addedAt").gte(filter.getAddedAtFrom()));
            } else if (filter.getAddedAtTo() != null) {
                criterias.add(Criteria.where("addedAt").lte(filter.getAddedAtTo()));
            }
        }

        if (filter.getPriceFrom() != null && filter.getPriceTo() != null) {
            criterias.add(Criteria.where("salePrice")
                    .gte(filter.getPriceFrom())
                    .lte(filter.getPriceTo())
            );
        } else {
            if (filter.getPriceFrom() != null) {
                criterias.add(Criteria.where("salePrice").gte(filter.getPriceFrom()));
            } else if (filter.getPriceTo() != null) {
                criterias.add(Criteria.where("salePrice").lte(filter.getPriceTo()));
            }
        }

        if (filter.getHasDiscount() != null) {
            if (filter.getHasDiscount()) {
                criterias.add(AggregationExpressionCriteria.whereExpr(
                        ComparisonOperators.valueOf("$salePrice").lessThan("$regularPrice")
                ));
            } else {
                criterias.add(AggregationExpressionCriteria.whereExpr(
                        ComparisonOperators.valueOf("$salePrice").equalTo("$regularPrice")
                ));
            }
        }

        if (filter.getInStock() != null) {
            if (filter.getInStock()) {
                criterias.add(Criteria.where("quantityInStock").gt(0));
            } else {
                criterias.add(Criteria.where("quantityInStock").is(0));
            }
        }

        if (filter.getCategoriesId() != null && filter.getCategoriesId().length > 0) {
            criterias.add(Criteria.where("categoryId")
                    .in((Object[]) filter.getCategoriesId())
            );
        }

        if (criterias.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                new Criteria().andOperator(criterias.toArray(new Criteria[0]))
        );
    }

    public Optional<TextCriteria> buildTextCriteria(ProductFilter filter) {
        if (StringUtils.isNoneBlank(filter.getTerm())) {
//            #Using regex for search
//            String regexExpression = String.format(findWordRegex, filter.getTerm());
//             criterias.add(
//                    new Criteria().orOperator(
//                            Criteria.where("name").regex(regexExpression),
//                            Criteria.where("brand").regex(regexExpression),
//                            Criteria.where("description").regex(regexExpression)
//                    )
//            );

            return Optional.of(TextCriteria.forDefaultLanguage().matching(filter.getTerm()));
        }
        return Optional.empty();
    }

    private Sort sortWith(ProductFilter filter) {
        if (StringUtils.isNotBlank(filter.getTerm())){
            return Sort.by("score");
        }

        return Sort.by(
                filter.getSortDirectionOrDefault(),
                filter.getSortByPropertyOrDefault().getPropertyName()
        );
    }

//    Version 1
//    private Query queryWith(ProductFilter filter) {
//        Query query = new Query();
//
//        if (filter.getEnabled() != null) {
//            query.addCriteria(Criteria.where("enabled").is(filter.getEnabled()));
//        }
//
//        if (filter.getAddedAtFrom() != null && filter.getAddedAtTo() != null) {
//            query.addCriteria(Criteria.where("addedAt")
//                    .gte(filter.getAddedAtFrom())
//                    .lte(filter.getAddedAtTo())
//            );
//        } else {
//            if (filter.getAddedAtFrom() != null) {
//                query.addCriteria(Criteria.where("addedAt").gte(filter.getAddedAtFrom()));
//            } else if (filter.getAddedAtTo() != null) {
//                query.addCriteria(Criteria.where("addedAt").lte(filter.getAddedAtTo()));
//            }
//        }
//
//        if (filter.getPriceFrom() != null && filter.getPriceTo() != null) {
//            query.addCriteria(Criteria.where("salePrice")
//                    .gte(filter.getPriceFrom())
//                    .lte(filter.getPriceTo())
//            );
//        } else {
//            if (filter.getPriceFrom() != null) {
//                query.addCriteria(Criteria.where("salePrice").gte(filter.getPriceFrom()));
//            } else if (filter.getPriceTo() != null) {
//                query.addCriteria(Criteria.where("salePrice").lte(filter.getPriceTo()));
//            }
//        }
//
//        if (filter.getHasDiscount() != null) {
//            if (filter.getHasDiscount()) {
//                query.addCriteria(AggregationExpressionCriteria.whereExpr(
//                        ComparisonOperators.valueOf("$salePrice").lessThan("$regularPrice")
//                ));
//            }else{
//                query.addCriteria(AggregationExpressionCriteria.whereExpr(
//                        ComparisonOperators.valueOf("$salePrice").equalTo("$regularPrice")
//                ));
//            }
//        }
//
//        if (filter.getInStock() != null) {
//            if (filter.getInStock()) {
//                query.addCriteria(Criteria.where("quantityInStock").gt(0));
//            } else {
//                query.addCriteria(Criteria.where("quantityInStock").is(0));
//            }
//        }
//
//        if (filter.getCategoriesId() != null && filter.getCategoriesId().length > 0) {
//            query.addCriteria(Criteria.where("categoryId")
//                    .in((Object[]) filter.getCategoriesId())
//            );
//        }
//
//        if (StringUtils.isNoneBlank(filter.getTerm())){
////            #Using regex for search
////            String regexExpression = String.format(findWordRegex, filter.getTerm());
////            query.addCriteria(
////                    new Criteria().orOperator(
////                            Criteria.where("name").regex(regexExpression),
////                            Criteria.where("brand").regex(regexExpression),
////                            Criteria.where("description").regex(regexExpression)
////                    )
////            );
//
//            query.addCriteria(
//                    TextCriteria.forDefaultLanguage().matching(filter.getTerm())
//            );
//        }
//
//        return query;
//    }
}