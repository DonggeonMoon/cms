package com.dgmoonlabs.cms.domain.business.category.service;

import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.Spec;
import com.dgmoonlabs.cms.domain.business.category.entity.CategoryAttribute;
import com.dgmoonlabs.cms.domain.business.category.entity.ProductAttributeValue;
import com.dgmoonlabs.cms.domain.business.category.repository.ProductAttributeValueRepository;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.global.error.ErrorResponse.FieldError;
import com.dgmoonlabs.cms.global.error.FieldValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductAttributeService {
    private static final int MAX_TEXT = 200;

    private final CategoryService categoryService;
    private final ProductAttributeValueRepository valueRepository;

    /** 카테고리에 정의되지 않은 속성은 버리고, 카테고리를 바꾸면 이전 카테고리 속성 값도 지운다. */
    @Transactional
    public void save(final Product product, final Map<String, Object> input) {
        valueRepository.deleteAllByProductId(product.getId());
        if (product.getCategoryId() == null || input == null) {
            return;
        }
        List<FieldError> errors = new ArrayList<>();
        List<ProductAttributeValue> values = new ArrayList<>();
        for (CategoryAttribute attribute : categoryService.effectiveAttributes(product.getCategoryId())) {
            Object raw = input.get(attribute.key());
            if (raw == null || String.valueOf(raw).isBlank()) {
                continue;
            }
            String text = String.valueOf(raw).trim();
            ProductAttributeValue.ProductAttributeValueBuilder value = ProductAttributeValue.builder()
                    .productId(product.getId()).attributeKey(attribute.key());
            switch (attribute.type()) {
                case SELECT -> {
                    if (!attribute.optionsOrEmpty().contains(text)) {
                        errors.add(new FieldError("attributes." + attribute.key(), attribute.label() + " 값은 선택지 중에서 골라 주세요."));
                        continue;
                    }
                    value.textValue(text);
                }
                case NUMBER -> {
                    try {
                        value.numberValue(new BigDecimal(text));
                    } catch (NumberFormatException e) {
                        errors.add(new FieldError("attributes." + attribute.key(), attribute.label() + " 값은 숫자여야 합니다."));
                        continue;
                    }
                }
                case TEXT -> {
                    if (text.length() > MAX_TEXT) {
                        errors.add(new FieldError("attributes." + attribute.key(), attribute.label() + " 값이 너무 깁니다."));
                        continue;
                    }
                    value.textValue(text);
                }
            }
            values.add(value.build());
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
        valueRepository.saveAll(values);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> values(final Long productId) {
        Map<String, Object> values = new LinkedHashMap<>();
        valueRepository.findAllByProductId(productId).forEach(value -> values.put(value.getAttributeKey(), value.value()));
        return values;
    }

    @Transactional(readOnly = true)
    public List<Spec> specs(final Product product) {
        if (product.getCategoryId() == null) {
            return List.of();
        }
        Map<String, ProductAttributeValue> values = valueRepository.findAllByProductId(product.getId()).stream()
                .collect(Collectors.toMap(ProductAttributeValue::getAttributeKey, Function.identity(), (a, b) -> a));
        return categoryService.effectiveAttributes(product.getCategoryId()).stream()
                .filter(attribute -> values.containsKey(attribute.key()))
                .map(attribute -> new Spec(attribute.key(), attribute.label(), values.get(attribute.key()).value(), attribute.unit()))
                .toList();
    }
}
