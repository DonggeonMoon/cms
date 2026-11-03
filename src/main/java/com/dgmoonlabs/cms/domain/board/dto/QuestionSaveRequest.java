package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record QuestionSaveRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 100_000) String contentHtml,
        @MatchesRegex(RegexTarget.EMAIL) String email,
        @Size(max = 30) @MatchesRegex(RegexTarget.PHONE_NUMBER) String phoneNumber,
        boolean isSecret,
        @Size(max = 50) String authorName,
        @Size(max = 64) String password
) {
}
