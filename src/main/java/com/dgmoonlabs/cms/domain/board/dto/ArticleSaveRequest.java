package com.dgmoonlabs.cms.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ArticleSaveRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 200_000) String contentHtml,
        boolean isSecret,
        boolean isNotice,
        LocalDateTime noticeStartDateTime,
        LocalDateTime noticeEndDateTime,
        boolean isHidden,
        @Size(max = 50) String authorName,
        @Size(max = 64) String password,
        @Size(max = 20) List<Long> fileIds
) {
    public List<Long> fileIdsOrEmpty() {
        return fileIds == null ? List.of() : fileIds;
    }
}
