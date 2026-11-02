package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password,
        UserType userType
) {
    public UserType userTypeOrDefault() {
        return userType == null ? UserType.MEMBER : userType;
    }
}
