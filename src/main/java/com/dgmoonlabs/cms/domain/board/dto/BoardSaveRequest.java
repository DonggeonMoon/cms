package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BoardSaveRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @NotNull BoardType type,
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
        @Min(0) @Max(20) int maximumFileCount,
        @Min(1) @Max(100) int pageSize
) {
}
