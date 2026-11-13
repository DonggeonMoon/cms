package com.dgmoonlabs.cms.domain.admin.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public final class LoginSecurityDtos {
    private LoginSecurityDtos() {
    }

    public record LoginSecuritySaveRequest(@Min(1) @Max(1000) int maxLoginTryCount) {
    }

    public record LoginSecurityResponse(int maxLoginTryCount) {
    }
}
