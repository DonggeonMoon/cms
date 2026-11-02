package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;

import java.time.LocalDateTime;

public record ArticleSummaryResponse(
        Long id,
        String title,
        String authorName,
        boolean isNotice,
        boolean isSecret,
        boolean isHidden,
        int hit,
        int likes,
        long commentCount,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
    public static ArticleSummaryResponse of(final BoardArticle article, final long commentCount, final String thumbnailUrl) {
        return new ArticleSummaryResponse(
                article.getId(),
                article.getTitle(),
                article.getAuthorName(),
                article.isNotice(),
                article.isSecret(),
                article.isHidden(),
                article.getHit(),
                article.getLikes(),
                commentCount,
                thumbnailUrl,
                article.getCreatedAt()
        );
    }
}
