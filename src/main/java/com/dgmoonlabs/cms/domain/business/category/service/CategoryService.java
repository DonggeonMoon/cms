package com.dgmoonlabs.cms.domain.business.category.service;

import com.dgmoonlabs.cms.domain.application.repository.ApplicationFormRepository;
import com.dgmoonlabs.cms.domain.business.category.constant.AttributeType;
import com.dgmoonlabs.cms.domain.business.category.constant.CategoryDomain;
import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.CategoryResponse;
import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.CategorySaveRequest;
import com.dgmoonlabs.cms.domain.business.category.entity.Category;
import com.dgmoonlabs.cms.domain.business.category.entity.CategoryAttribute;
import com.dgmoonlabs.cms.domain.business.category.repository.CategoryRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationItemRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseRepository;
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
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(final CategoryDomain domain) {
        List<Category> categories = categoryRepository.findAllByDomainAndDeletedAtIsNullOrderBySequenceAscIdAsc(domain);
        Map<Long, Category> byId = categories.stream().collect(Collectors.toMap(Category::getId, Function.identity()));
        return categories.stream()
                .map(category -> CategoryResponse.of(category, effective(category, byId.get(category.getParentId())),
                        usageCount(category)))
                .toList();
    }

    @Transactional
    public Long create(final CategorySaveRequest request) {
        Category parent = validParent(request.domain(), request.parentId(), null);
        validateAttributes(parent, request.attributesOrEmpty());
        return categoryRepository.save(Category.builder()
                .domain(request.domain())
                .name(request.name().trim())
                .parentId(parent == null ? null : parent.getId())
                .sequence(request.sequence() == null ? 0 : request.sequence())
                .attributes(request.attributesOrEmpty())
                .build()).getId();
    }

    @Transactional
    public void update(final Long id, final CategorySaveRequest request) {
        Category category = find(id);
        if (category.getDomain() != request.domain()) {
            throw new BusinessException(ErrorCode.CATEGORY_DOMAIN_MISMATCH);
        }
        Category parent = validParent(request.domain(), request.parentId(), category);
        validateAttributes(parent, request.attributesOrEmpty());
        if (parent == null) {
            List<String> own = request.attributesOrEmpty().stream().map(CategoryAttribute::key).toList();
            boolean clashesWithChild = categoryRepository.findAllByDomainAndDeletedAtIsNullOrderBySequenceAscIdAsc(request.domain()).stream()
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
        Category category = find(id);
        if (categoryRepository.existsByParentIdAndDeletedAtIsNull(id) || usageCount(category) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        category.delete();
    }

    @Transactional(readOnly = true)
    public List<CategoryAttribute> effectiveAttributes(final Long categoryId) {
        Category category = find(categoryId);
        Category parent = category.getParentId() == null ? null
                : categoryRepository.findByIdAndDeletedAtIsNull(category.getParentId()).orElse(null);
        return effective(category, parent);
    }

    /** 상위 카테고리로 찾으면 하위 카테고리 항목까지 나오도록 자신과 하위 카테고리 ID를 돌려준다. */
    @Transactional(readOnly = true)
    public List<Long> selfAndChildIds(final Long categoryId) {
        Category category = find(categoryId);
        return Stream.concat(Stream.of(categoryId), categoryRepository.findAllByDomainAndDeletedAtIsNullOrderBySequenceAscIdAsc(category.getDomain()).stream()
                .filter(c -> categoryId.equals(c.getParentId())).map(Category::getId)).toList();
    }

    public Category find(final Long id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    /** 상품·예약 상품·신청 폼·스트리밍/교육과정에 카테고리를 연결할 때 용도가 맞는 카테고리인지 함께 확인한다. */
    public Category findForDomain(final Long id, final CategoryDomain domain) {
        Category category = find(id);
        if (category.getDomain() != domain) {
            throw new BusinessException(ErrorCode.CATEGORY_DOMAIN_MISMATCH);
        }
        return category;
    }

    private long usageCount(final Category category) {
        return switch (category.getDomain()) {
            case PRODUCT -> productRepository.countByCategoryIdAndDeletedAtIsNull(category.getId());
            case RESERVATION_ITEM -> reservationItemRepository.countByCategoryIdAndDeletedAtIsNull(category.getId());
            case APPLICATION -> applicationFormRepository.countByCategoryIdAndDeletedAtIsNull(category.getId());
            case COURSE -> courseRepository.countByCategoryIdAndDeletedAtIsNull(category.getId());
        };
    }

    private static List<CategoryAttribute> effective(final Category category, final Category parent) {
        return parent == null ? category.attributesOrEmpty()
                : Stream.concat(parent.attributesOrEmpty().stream(), category.attributesOrEmpty().stream()).toList();
    }

    /** 카테고리는 두 단계까지만 둔다: 상위는 최상위여야 하고, 하위가 있는 카테고리는 다른 카테고리 아래로 갈 수 없다. 상위·하위는 같은 용도(domain)여야 한다. */
    private Category validParent(final CategoryDomain domain, final Long parentId, final Category self) {
        if (parentId == null) {
            return null;
        }
        Category parent = find(parentId);
        boolean invalid = parent.getDomain() != domain || parent.getParentId() != null
                || (self != null && (self.getId().equals(parentId) || categoryRepository.existsByParentIdAndDeletedAtIsNull(self.getId())));
        if (invalid) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_PARENT);
        }
        return parent;
    }

    private static void validateAttributes(final Category parent, final List<CategoryAttribute> attributes) {
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
