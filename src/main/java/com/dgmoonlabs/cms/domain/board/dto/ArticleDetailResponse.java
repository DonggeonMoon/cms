package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleDetailResponse(
        Long id,
        Long boardId,
        String title,
        String contentHtml,
        String authorName,
        UserType authorType,
        boolean isNotice,
        LocalDateTime noticeStartDateTime,
        LocalDateTime noticeEndDateTime,
        boolean isSecret,
        boolean isHidden,
        int hit,
        int likes,
        List<FileResponse> files,
        boolean canModify,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static ArticleDetailResponse of(final BoardArticle article, final List<FileResponse> files, final boolean canModify) {
        return new ArticleDetailResponse(
                article.getId(),
                article.getBoardId(),
                article.getTitle(),
                article.getContentHtml(),
                article.getAuthorName(),
                article.getUserType(),
                article.isNotice(),
                article.getNoticeStartDateTime(),
                article.getNoticeEndDateTime(),
                article.isSecret(),
                article.isHidden(),
                article.getHit(),
                article.getLikes(),
                files,
                canModify,
                article.getCreatedAt(),
                article.getModifiedAt()
        );
    }
}
