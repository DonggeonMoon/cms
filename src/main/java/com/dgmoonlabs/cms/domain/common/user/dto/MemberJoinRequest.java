package com.dgmoonlabs.cms.domain.common.user.dto;

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
        @NotBlank @Pattern(regexp = "^[a-z0-9_]{4,20}$", message = "아이디는 영문 소문자, 숫자, _ 로 4~20자여야 합니다.") String username,
        @NotBlank @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.") String password,
        @NotBlank String passwordCheck,
        @NotBlank @Size(max = 50) String name,
        String lastName,
        String firstName,
        LocalDate birthDate,
        Boolean isLunarBirthDate,
        String gender,
        @Email String email,
        String telephoneNumber,
        String mobilePhoneNumber,
        String postalCode,
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
    public Member toEntity(final String encodedPassword, final MemberName memberName, final String nickname,
                           final MemberJoinSetting setting) {
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
