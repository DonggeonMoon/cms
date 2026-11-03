package com.dgmoonlabs.cms.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentEditRequest(
        @NotBlank @Size(max = 2000) String content,
        @Size(max = 64) String password
) {
}
