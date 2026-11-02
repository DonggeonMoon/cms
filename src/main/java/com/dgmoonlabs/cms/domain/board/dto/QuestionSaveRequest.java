package com.dgmoonlabs.cms.domain.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record QuestionSaveRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 100_000) String contentHtml,
        @Email String email,
        boolean isSecret,
        @Size(max = 50) String authorName,
        @Size(max = 64) String password
) {
}
