package com.dgmoonlabs.cms.domain.common.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class IdentityVerificationDtos {
    private IdentityVerificationDtos() {
    }

    public record StartRequest(@NotBlank @Size(max = 500) String returnUrl) {
    }

    public record CompleteRequest(@NotBlank @Size(max = 200) String token) {
    }

    /** 인증 수단(앱·웹)으로 보낼 때 필요한 정보. */
    public record VerificationSession(String redirectUrl, String token) {
    }

    /** CI(연계정보)·DI(중복가입확인정보)는 인증사마다 포맷이 달라 문자열로만 다룬다. */
    public record VerificationResult(String connectionInfo, String duplicationInfo, String name, LocalDate birthDate,
                                      String gender, String phoneNumber, boolean foreigner) {
    }

    public record EnabledRequest(@NotNull Boolean enabled) {
    }

    public record SettingsResponse(boolean enabled) {
    }
}
