package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.domain.board.entity.comment.BoardComment;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long parentId,
        int depth,
        String content,
        String authorName,
        UserType authorType,
        CommentStatus status,
        boolean deleted,
        boolean canDelete,
        LocalDateTime createdAt
) {
    private static final String REMOVED = "삭제된 댓글입니다.";
    private static final String MODERATED = "관리자에 의해 숨겨진 댓글입니다.";

    public static CommentResponse of(final BoardComment comment, final boolean canDelete) {
        boolean deleted = comment.getDeletedAt() != null;
        boolean moderated = comment.getStatus() == CommentStatus.MODERATED;
        return new CommentResponse(
                comment.getId(),
                comment.getParentId(),
                comment.getDepth(),
                deleted ? REMOVED : moderated ? MODERATED : comment.getContent(),
                deleted ? null : comment.getAuthorName(),
                comment.getUserType(),
                comment.getStatus(),
                deleted,
                canDelete && !deleted,
                comment.getCreatedAt()
        );
    }
}
