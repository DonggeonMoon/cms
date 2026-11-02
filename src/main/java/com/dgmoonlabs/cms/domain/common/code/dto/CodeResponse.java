package com.dgmoonlabs.cms.domain.common.code.dto;

import com.dgmoonlabs.cms.domain.common.code.entity.Code;

public record CodeResponse(
        Long id,
        Long parentId,
        int depth,
        int order,
        String name,
        String englishName,
        String description,
        boolean isUsing
) {
    public static CodeResponse from(final Code code) {
        return new CodeResponse(
                code.getId(),
                code.getParentId(),
                code.getDepth(),
                code.getOrder(),
                code.getName(),
                code.getEnglishName(),
                code.getDescription(),
                code.isUsing()
        );
    }
}
