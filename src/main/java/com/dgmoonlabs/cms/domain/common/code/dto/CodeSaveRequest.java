package com.dgmoonlabs.cms.domain.common.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CodeSaveRequest(
        Long parentId,
        @Positive Integer order,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String englishName,
        @Size(max = 255) String description,
        boolean isUsing
) {
    public CodeSaveRequest {
        order = order == null ? 1 : order;
    }
}
