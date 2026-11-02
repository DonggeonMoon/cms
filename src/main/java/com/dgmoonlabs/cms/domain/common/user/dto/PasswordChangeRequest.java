package com.dgmoonlabs.cms.domain.common.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.") String newPassword,
        @NotBlank String newPasswordCheck
) {
}
