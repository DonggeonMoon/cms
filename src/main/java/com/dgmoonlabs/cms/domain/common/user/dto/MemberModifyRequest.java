package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MemberModifyRequest(
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
        String officeDetailAddress
) {
}
