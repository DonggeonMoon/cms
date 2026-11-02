package com.dgmoonlabs.cms.domain.common.code.dto;

import lombok.Builder;

@Builder
public record CodeRequest(
        Long parentId,
        Boolean rootOnly,
        String keyword,
        Boolean isUsing
) {
}
