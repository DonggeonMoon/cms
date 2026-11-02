package com.dgmoonlabs.cms.domain.common.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ReauthenticationRequest(@NotBlank String password) {
}
