package com.dgmoonlabs.cms.domain.business.category.service;

import com.dgmoonlabs.cms.domain.business.category.constant.AttributeType;
import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.CategoryResponse;
import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.CategorySaveRequest;
import com.dgmoonlabs.cms.domain.business.category.entity.CategoryAttribute;
import com.dgmoonlabs.cms.domain.business.category.entity.ProductCategory;
import com.dgmoonlabs.cms.domain.business.category.repository.ProductCategoryRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        List<ProductCategory> categories = categoryRepository.findAllByDeletedAtIsNullOrderBySequenceAscIdAsc();
        Map<Long, ProductCategory> byId = categories.stream().collect(Collectors.toMap(ProductCategory::getId, Function.identity()));
        return categories.stream()
                .map(category -> CategoryResponse.of(category, effective(category, byId.get(category.getParentId())),
                        productRepository.countByCategoryIdAndDeletedAtIsNull(category.getId())))
                .toList();
    }

    @Transactional
    public Long create(final CategorySaveRequest request) {
        ProductCategory parent = validParent(request.parentId(), null);
        validateAttributes(parent, request.attributesOrEmpty());
        return categoryRepository.save(ProductCategory.builder()
                .name(request.name().trim())
                .parentId(parent == null ? null : parent.getId())
                .sequence(request.sequence() == null ? 0 : request.sequence())
                .attributes(request.attributesOrEmpty())
                .build()).getId();
    }

    @Transactional
    public void update(final Long id, final CategorySaveRequest request) {
        ProductCategory category = find(id);
        ProductCategory parent = validParent(request.parentId(), category);
        validateAttributes(parent, request.attributesOrEmpty());
        if (parent == null) {
            List<String> own = request.attributesOrEmpty().stream().map(CategoryAttribute::key).toList();
            boolean clashesWithChild = categoryRepository.findAllByDeletedAtIsNullOrderBySequenceAscIdAsc().stream()
                    .filter(child -> id.equals(child.getParentId()))
                    .flatMap(child -> child.attributesOrEmpty().stream())
                    .anyMatch(attribute -> own.contains(attribute.key()));
            if (clashesWithChild) {
                throw new BusinessException(ErrorCode.INVALID_CATEGORY_ATTRIBUTES);
            }
        }
        category.update(request.name().trim(), parent == null ? null : parent.getId(),
                request.sequence() == null ? 0 : request.sequence(), request.attributesOrEmpty());
    }

    @Transactional
    public void delete(final Long id) {
        ProductCategory category = find(id);
        if (categoryRepository.existsByParentIdAndDeletedAtIsNull(id) || productRepository.countByCategoryIdAndDeletedAtIsNull(id) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        category.delete();
    }

    @Transactional(readOnly = true)
    public List<CategoryAttribute> effectiveAttributes(final Long categoryId) {
        ProductCategory category = find(categoryId);
        ProductCategory parent = category.getParentId() == null ? null
                : categoryRepository.findByIdAndDeletedAtIsNull(category.getParentId()).orElse(null);
        return effective(category, parent);
    }

    /** 상위 카테고리로 찾으면 하위 카테고리 상품까지 나오도록 자신과 하위 카테고리 ID를 돌려준다. */
    @Transactional(readOnly = true)
    public List<Long> selfAndChildIds(final Long categoryId) {
        find(categoryId);
        return Stream.concat(Stream.of(categoryId), categoryRepository.findAllByDeletedAtIsNullOrderBySequenceAscIdAsc().stream()
                .filter(category -> categoryId.equals(category.getParentId())).map(ProductCategory::getId)).toList();
    }

    public ProductCategory find(final Long id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static List<CategoryAttribute> effective(final ProductCategory category, final ProductCategory parent) {
        return parent == null ? category.attributesOrEmpty()
                : Stream.concat(parent.attributesOrEmpty().stream(), category.attributesOrEmpty().stream()).toList();
    }

    /** 카테고리는 두 단계까지만 둔다: 상위는 최상위여야 하고, 하위가 있는 카테고리는 다른 카테고리 아래로 갈 수 없다. */
    private ProductCategory validParent(final Long parentId, final ProductCategory self) {
        if (parentId == null) {
            return null;
        }
        ProductCategory parent = find(parentId);
        boolean invalid = parent.getParentId() != null
                || (self != null && (self.getId().equals(parentId) || categoryRepository.existsByParentIdAndDeletedAtIsNull(self.getId())));
        if (invalid) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_PARENT);
        }
        return parent;
    }

    private static void validateAttributes(final ProductCategory parent, final List<CategoryAttribute> attributes) {
        Set<String> keys = new HashSet<>();
        if (parent != null) {
            parent.attributesOrEmpty().forEach(attribute -> keys.add(attribute.key()));
        }
        for (CategoryAttribute attribute : attributes) {
            if (!keys.add(attribute.key()) || (attribute.type() == AttributeType.SELECT && attribute.optionsOrEmpty().isEmpty())) {
                throw new BusinessException(ErrorCode.INVALID_CATEGORY_ATTRIBUTES);
            }
        }
    }
}
