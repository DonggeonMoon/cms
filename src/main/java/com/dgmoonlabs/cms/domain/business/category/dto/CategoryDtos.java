package com.dgmoonlabs.cms.domain.business.category.dto;

import com.dgmoonlabs.cms.domain.business.category.constant.CategoryDomain;
import com.dgmoonlabs.cms.domain.business.category.entity.Category;
import com.dgmoonlabs.cms.domain.business.category.entity.CategoryAttribute;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CategoryDtos {
    private CategoryDtos() {
    }

    public record CategorySaveRequest(@NotNull CategoryDomain domain, @NotBlank @Size(max = 50) String name, Long parentId,
                                      Integer sequence, @Size(max = 30) List<@Valid CategoryAttribute> attributes) {
        public List<CategoryAttribute> attributesOrEmpty() {
            return attributes == null ? List.of() : attributes;
        }
    }

    /** attributes 는 이 카테고리에서 정의한 것, effectiveAttributes 는 상위 카테고리 것까지 합친 것. itemCount 는 도메인에 따라 상품·예약 상품·신청 폼·스트리밍/교육과정 수. */
    public record CategoryResponse(Long id, CategoryDomain domain, String name, Long parentId, int sequence,
                                   List<CategoryAttribute> attributes, List<CategoryAttribute> effectiveAttributes, long itemCount) {
        public static CategoryResponse of(final Category category, final List<CategoryAttribute> effective, final long itemCount) {
            return new CategoryResponse(category.getId(), category.getDomain(), category.getName(), category.getParentId(),
                    category.getSequence(), category.attributesOrEmpty(), effective, itemCount);
        }
    }

    public record Spec(String key, String label, Object value, String unit) {
    }
}
