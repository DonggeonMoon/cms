package com.dgmoonlabs.cms.domain.archive.dto;

import com.dgmoonlabs.cms.domain.archive.constant.ArchiveStatus;
import com.dgmoonlabs.cms.domain.archive.entity.article.Archive;
import com.dgmoonlabs.cms.domain.archive.entity.article.ArchiveArticle;
import com.dgmoonlabs.cms.domain.archive.entity.comment.ArchiveComment;
import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public final class ArchiveDtos {
    private ArchiveDtos() {
    }

    @Builder
    public record ArchiveSaveRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 255) String description,
            @NotNull BoardType type,
            boolean supportsComment,
            boolean supportsLike,
            @Min(1) @Max(100) int pageSize
    ) {
    }

    public record ArchiveResponse(Long id, String name, String description, BoardType type, boolean supportsComment,
                                  boolean supportsLike, int pageSize) {
        public static ArchiveResponse from(final Archive archive) {
            return new ArchiveResponse(archive.getId(), archive.getName(), archive.getDescription(), archive.getType(),
                    archive.isSupportsComment(), archive.isSupportsLike(), archive.getPageSize());
        }
    }

    @Builder
    public record ArchiveArticleSaveRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 200_000) String contentHtml,
            @NotNull ArchiveStatus status,
            @Size(max = 20) List<Long> fileIds
    ) {
        public List<Long> fileIdsOrEmpty() {
            return fileIds == null ? List.of() : fileIds;
        }
    }

    public record ArchiveArticleSummary(Long id, String title, String authorName, ArchiveStatus status, int hit,
                                        String thumbnailUrl, LocalDateTime createdAt) {
        public static ArchiveArticleSummary of(final ArchiveArticle article, final String thumbnailUrl) {
            return new ArchiveArticleSummary(article.getId(), article.getTitle(), article.getAuthorName(),
                    article.getStatus(), article.getHit(), thumbnailUrl, article.getCreatedAt());
        }
    }

    public record ArchiveArticleDetail(Long id, Long archiveId, String title, String contentHtml, String authorName,
                                       ArchiveStatus status, int hit, int likes, List<FileResponse> files, LocalDateTime createdAt,
                                       LocalDateTime modifiedAt) {
        public static ArchiveArticleDetail of(final ArchiveArticle article, final List<FileResponse> files) {
            return new ArchiveArticleDetail(article.getId(), article.getArchiveId(), article.getTitle(),
                    article.getContentHtml(), article.getAuthorName(), article.getStatus(), article.getHit(),
                    article.getLikes(), files,
                    article.getCreatedAt(), article.getModifiedAt());
        }
    }

    public record ArchiveCommentSaveRequest(Long parentId, @NotBlank @Size(max = 2000) String content) {
    }

    public record ArchiveCommentEditRequest(@NotBlank @Size(max = 2000) String content) {
    }

    public record ArchiveCommentResponse(Long id, Long parentId, int depth, String content, String authorName, boolean deleted,
                                         boolean canDelete, boolean canEdit, LocalDateTime createdAt) {
        private static final String REMOVED = "삭제된 댓글입니다.";

        public static ArchiveCommentResponse of(final ArchiveComment comment, final boolean canDelete, final boolean canEdit) {
            boolean deleted = comment.getDeletedAt() != null;
            return new ArchiveCommentResponse(comment.getId(), comment.getParentId(), comment.getDepth(),
                    deleted ? REMOVED : comment.getContent(), deleted ? null : comment.getAuthorName(), deleted,
                    canDelete && !deleted, canEdit && !deleted, comment.getCreatedAt());
        }
    }
}
