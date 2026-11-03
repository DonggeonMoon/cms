package com.dgmoonlabs.cms.domain.common.social.dto;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.domain.common.social.entity.SocialLoginSetting;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class SocialDtos {
    private SocialDtos() {
    }

    public record ProviderOption(SocialProvider provider, String path, String label) {
        public static ProviderOption of(final SocialProvider provider) {
            return new ProviderOption(provider, provider.path(), provider.getLabel());
        }
    }

    public record SocialLoginSettingRequest(
            boolean enabled,
            Long clientIdKeyId,
            Long clientSecretKeyId,
            @Size(max = 20) String appleTeamId,
            @Size(max = 20) String appleKeyId,
            Long applePrivateKeyKeyId
    ) {
    }

    public record SocialLoginSettingResponse(SocialProvider provider, String label, boolean enabled, boolean configured,
                                             Long clientIdKeyId, Long clientSecretKeyId, String appleTeamId,
                                             String appleKeyId, Long applePrivateKeyKeyId, String redirectUri,
                                             String consoleUrl) {
        public static SocialLoginSettingResponse of(final SocialLoginSetting setting, final String redirectUri) {
            SocialProvider provider = setting.getProvider();
            return new SocialLoginSettingResponse(provider, provider.getLabel(), setting.isEnabled(), setting.isConfigured(),
                    setting.getClientIdKeyId(), setting.getClientSecretKeyId(), setting.getAppleTeamId(),
                    setting.getAppleKeyId(), setting.getApplePrivateKeyKeyId(), redirectUri, provider.getConsoleUrl());
        }
    }

    public record PendingSignupResponse(SocialProvider provider, String label, String email, String name, String nickname,
                                        String mobilePhoneNumber, String suggestedUsername) {
    }

    public record SocialJoinRequest(
            @NotBlank @MatchesRegex(RegexTarget.USERNAME) String username,
            @Size(max = 30) @MatchesRegex(RegexTarget.NICKNAME) String nickname,
            @Size(max = 50) String name,
            @Size(max = 25) String lastName,
            @Size(max = 25) String firstName,
            LocalDate birthDate,
            @Pattern(regexp = "^(남|여)?$", message = "성별은 남 또는 여 중에서 골라 주세요.") String gender,
            @MatchesRegex(RegexTarget.EMAIL) String email,
            @MatchesRegex(RegexTarget.PHONE_NUMBER) String telephoneNumber,
            @MatchesRegex(RegexTarget.MOBILE_PHONE) String mobilePhoneNumber,
            @MatchesRegex(RegexTarget.POSTAL_CODE) String postalCode,
            String address,
            String detailAddress,
            boolean isMailReceptionAgreed,
            boolean isSmsReceptionAgreed,
            boolean isMarketingUsageAgreed,
            String officeName,
            String officeDepartment,
            String officePosition,
            String officeAddress,
            String officeDetailAddress,
            boolean isPrivacyConsentAgreed
    ) {
        public MemberJoinRequest toJoinRequest(final String unusablePassword) {
            return MemberJoinRequest.builder()
                    .username(username).password(unusablePassword).passwordCheck(unusablePassword)
                    .nickname(nickname).name(name).lastName(lastName).firstName(firstName).birthDate(birthDate)
                    .gender(gender).email(email).telephoneNumber(telephoneNumber).mobilePhoneNumber(mobilePhoneNumber)
                    .postalCode(postalCode).address(address).detailAddress(detailAddress)
                    .isMailReceptionAgreed(isMailReceptionAgreed).isSmsReceptionAgreed(isSmsReceptionAgreed)
                    .isMarketingUsageAgreed(isMarketingUsageAgreed).officeName(officeName)
                    .officeDepartment(officeDepartment).officePosition(officePosition).officeAddress(officeAddress)
                    .officeDetailAddress(officeDetailAddress).isPrivacyConsentAgreed(isPrivacyConsentAgreed)
                    .build();
        }
    }

    public record SignupResult(String redirect) {
    }
}
