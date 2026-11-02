package com.dgmoonlabs.cms.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CommentSaveRequest(
        Long parentId,
        @NotBlank @Size(max = 2000) String content,
        @Size(max = 50) String authorName,
        @Size(max = 64) String password
) {
}
