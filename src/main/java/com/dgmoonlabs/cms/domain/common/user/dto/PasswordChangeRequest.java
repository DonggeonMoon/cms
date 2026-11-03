package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(max = 64, message = "비밀번호는 64자 이하여야 합니다.") @MatchesRegex(RegexTarget.PASSWORD) String newPassword,
        @NotBlank String newPasswordCheck
) {
}
