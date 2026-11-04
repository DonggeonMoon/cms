package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardConfig;

public record BoardResponse(
        Long id,
        String name,
        String description,
        BoardType type,
        boolean isMemberAccessible,
        boolean isMemberReadable,
        boolean isMemberWritable,
        boolean isGuestAccessible,
        boolean isGuestReadable,
        boolean isGuestWritable,
        boolean supportsNotice,
        boolean supportsSecret,
        boolean supportsComment,
        boolean supportsLike,
        boolean supportsAttachment,
        int maximumFileCount,
        int pageSize,
        Long readGradeId,
        Long writeGradeId,
        Permissions permissions
) {
    public record Permissions(boolean canList, boolean canRead, boolean canWrite, boolean isAdmin) {
    }

    public static BoardResponse of(final Board board, final BoardConfig config, final Permissions permissions) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getDescription(),
                board.getType(),
                config.isMemberAccessible(),
                config.isMemberReadable(),
                config.isMemberWritable(),
                config.isGuestAccessible(),
                config.isGuestReadable(),
                config.isGuestWritable(),
                config.isSupportsNotice(),
                config.isSupportsSecret(),
                config.isSupportsComment(),
                config.isSupportsLike(),
                config.isSupportsAttachment(),
                config.getMaximumFileCount(),
                config.getPageSize(),
                config.getReadGradeId(),
                config.getWriteGradeId(),
                permissions
        );
    }
}
