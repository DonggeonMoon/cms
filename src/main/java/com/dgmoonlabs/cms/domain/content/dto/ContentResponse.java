package com.dgmoonlabs.cms.domain.content.dto;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.domain.content.entity.Content;

import java.time.LocalDateTime;

public record ContentResponse(
        Long id,
        String title,
        String contentHtml,
        ContentStatus status,
        int version,
        String lastWorker,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long accessGradeId
) {
    public static ContentResponse from(final Content content) {
        return new ContentResponse(content.getId(), content.getTitle(), content.getContentHtml(), content.getStatus(),
                content.getVersion(), content.getLastWorker(), content.getMemo(), content.getCreatedAt(),
                content.getModifiedAt(), content.getAccessGradeId());
    }
}
