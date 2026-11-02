package com.dgmoonlabs.cms.domain.common.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CodeSaveRequest(
        Long parentId,
        @PositiveOrZero int order,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String englishName,
        @Size(max = 255) String description,
        boolean isUsing
) {
}
