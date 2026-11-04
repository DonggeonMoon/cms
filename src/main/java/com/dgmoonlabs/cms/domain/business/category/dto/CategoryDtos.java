package com.dgmoonlabs.cms.domain.business.category.dto;

import com.dgmoonlabs.cms.domain.business.category.entity.CategoryAttribute;
import com.dgmoonlabs.cms.domain.business.category.entity.ProductCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CategoryDtos {
    private CategoryDtos() {
    }

    public record CategorySaveRequest(@NotBlank @Size(max = 50) String name, Long parentId, Integer sequence,
                                      @Size(max = 30) List<@Valid CategoryAttribute> attributes) {
        public List<CategoryAttribute> attributesOrEmpty() {
            return attributes == null ? List.of() : attributes;
        }
    }

    /** attributes 는 이 카테고리에서 정의한 것, effectiveAttributes 는 상위 카테고리 것까지 합친 것. */
    public record CategoryResponse(Long id, String name, Long parentId, int sequence, List<CategoryAttribute> attributes,
                                   List<CategoryAttribute> effectiveAttributes, long productCount) {
        public static CategoryResponse of(final ProductCategory category, final List<CategoryAttribute> effective,
                                          final long productCount) {
            return new CategoryResponse(category.getId(), category.getName(), category.getParentId(), category.getSequence(),
                    category.attributesOrEmpty(), effective, productCount);
        }
    }

    public record Spec(String key, String label, Object value, String unit) {
    }
}
