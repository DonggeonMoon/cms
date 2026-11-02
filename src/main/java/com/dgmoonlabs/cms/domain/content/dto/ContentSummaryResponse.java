package com.dgmoonlabs.cms.domain.content.dto;

import com.dgmoonlabs.cms.domain.content.entity.Content;

public record ContentSummaryResponse(Long id, String title) {
    public static ContentSummaryResponse from(final Content content) {
        return new ContentSummaryResponse(content.getId(), content.getTitle());
    }
}
