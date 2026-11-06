package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.business.category.entity.ProductAttributeValue;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProductSearchRepositoryImpl implements ProductSearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> search(final ProductSearchCondition condition, final Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);
        query.select(product).where(predicates(cb, query, product, condition)).orderBy(cb.desc(product.get("id")));
        List<Product> content = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> count = cb.createQuery(Long.class);
        Root<Product> counted = count.from(Product.class);
        count.select(cb.count(counted)).where(predicates(cb, count, counted, condition));
        return new PageImpl<>(content, pageable, entityManager.createQuery(count).getSingleResult());
    }

    private static Predicate[] predicates(final CriteriaBuilder cb, final CriteriaQuery<?> query, final Root<Product> product,
                                          final ProductSearchCondition condition) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isNull(product.get("deletedAt")));
        if (condition.onlyOnSale()) {
            predicates.add(cb.isTrue(product.get("isEnabled")));
            predicates.add(cb.isNull(product.get("streamingId")));
            predicates.add(cb.isNull(product.get("articleId")));
        }
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            predicates.add(cb.like(cb.lower(product.get("name")), "%" + condition.keyword().toLowerCase() + "%"));
        }
        if (condition.categoryIds() != null) {
            predicates.add(product.get("categoryId").in(condition.categoryIds()));
        }
        for (Map.Entry<String, List<String>> choice : condition.choices().entrySet()) {
            predicates.add(cb.exists(attribute(cb, query, product, choice.getKey(),
                    value -> value.get("textValue").in(choice.getValue()))));
        }
        for (Map.Entry<String, BigDecimal[]> range : condition.ranges().entrySet()) {
            BigDecimal min = range.getValue()[0];
            BigDecimal max = range.getValue()[1];
            predicates.add(cb.exists(attribute(cb, query, product, range.getKey(), value -> {
                Path<BigDecimal> number = value.get("numberValue");
                return cb.and(min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(number, min),
                        max == null ? cb.conjunction() : cb.lessThanOrEqualTo(number, max));
            })));
        }
        return predicates.toArray(Predicate[]::new);
    }

    private static Subquery<Long> attribute(final CriteriaBuilder cb, final CriteriaQuery<?> query, final Root<Product> product,
                                            final String key,
                                            final Function<Root<ProductAttributeValue>, Predicate> matches) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<ProductAttributeValue> value = subquery.from(ProductAttributeValue.class);
        return subquery.select(value.get("id")).where(
                cb.equal(value.get("productId"), product.get("id")),
                cb.equal(value.get("attributeKey"), key),
                matches.apply(value));
    }
}
