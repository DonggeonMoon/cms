package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.policy.constant.PolicyType;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import com.dgmoonlabs.cms.domain.common.user.constant.PersonalInfoItem;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberJoinSetting;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAddress;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAgreement;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberOrganization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;
import java.util.function.Predicate;

@Builder
public record MemberJoinRequest(
        @NotBlank @MatchesRegex(RegexTarget.USERNAME) String username,
        @NotBlank @Size(max = 64, message = "비밀번호는 64자 이하여야 합니다.") @MatchesRegex(RegexTarget.PASSWORD) String password,
        @NotBlank String passwordCheck,
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
        boolean isPrivacyConsentAgreed,
        Long referredBy
) {
    public Member toEntity(final String encodedPassword, final MemberName memberName, final String nickname,
                           final MemberJoinSetting setting, final Long validReferredBy) {
        return Member.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(nickname)
                .name(memberName.name())
                .lastName(memberName.lastName())
                .firstName(memberName.firstName())
                .birthDate(setting.keep(PersonalInfoItem.BIRTH_DATE, birthDate))
                .gender(setting.keep(PersonalInfoItem.GENDER, gender))
                .email(setting.keep(PersonalInfoItem.EMAIL, email))
                .telephoneNumber(setting.keep(PersonalInfoItem.TELEPHONE, telephoneNumber))
                .mobilePhoneNumber(setting.keep(PersonalInfoItem.MOBILE_PHONE, mobilePhoneNumber))
                .termConfirmationDate(LocalDate.now())
                .referredByMemberId(validReferredBy)
                .build();
    }

    public MemberAddress toAddress(final Long memberId, final MemberJoinSetting setting) {
        return MemberAddress.builder()
                .memberId(memberId)
                .postalCode(setting.keep(PersonalInfoItem.ADDRESS, postalCode))
                .address(setting.keep(PersonalInfoItem.ADDRESS, address))
                .detailAddress(setting.keep(PersonalInfoItem.ADDRESS, detailAddress))
                .build();
    }

    public MemberAgreement toAgreement(final Long memberId, final Predicate<PolicyType> isUsing) {
        return MemberAgreement.builder()
                .memberId(memberId)
                .isMailReceptionAgreed(isMailReceptionAgreed && isUsing.test(PolicyType.MAIL_RECEPTION))
                .isSmsReceptionAgreed(isSmsReceptionAgreed && isUsing.test(PolicyType.SMS_RECEPTION))
                .isMarketingUsageAgreed(isMarketingUsageAgreed && isUsing.test(PolicyType.MARKETING_USAGE))
                .build();
    }

    public MemberOrganization toOrganization(final Long memberId, final MemberJoinSetting setting) {
        return MemberOrganization.builder()
                .memberId(memberId)
                .officeName(setting.keep(PersonalInfoItem.OFFICE, officeName))
                .officeDepartment(setting.keep(PersonalInfoItem.OFFICE, officeDepartment))
                .officePosition(setting.keep(PersonalInfoItem.OFFICE, officePosition))
                .officeAddress(setting.keep(PersonalInfoItem.OFFICE, officeAddress))
                .officeDetailAddress(setting.keep(PersonalInfoItem.OFFICE, officeDetailAddress))
                .build();
    }
}
