package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAddress;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAgreement;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberOrganization;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

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
    public Member toEntity(final String encodedPassword) {
        return Member.builder()
                .username(username)
                .password(encodedPassword)
                .name(name)
                .lastName(lastName)
                .firstName(firstName)
                .birthDate(birthDate)
                .isLunarBirthDate(Boolean.TRUE.equals(isLunarBirthDate))
                .gender(gender)
                .email(email)
                .telephoneNumber(telephoneNumber)
                .mobilePhoneNumber(mobilePhoneNumber)
                .termConfirmationDate(LocalDate.now())
                .build();
    }

    public MemberAddress toAddress(final Long memberId) {
        return MemberAddress.builder()
                .memberId(memberId)
                .postalCode(postalCode)
                .address(address)
                .detailAddress(detailAddress)
                .build();
    }

    public MemberAgreement toAgreement(final Long memberId) {
        return MemberAgreement.builder()
                .memberId(memberId)
                .isMailReceptionAgreed(isMailReceptionAgreed)
                .isSmsReceptionAgreed(isSmsReceptionAgreed)
                .isMarketingUsageAgreed(isMarketingUsageAgreed)
                .build();
    }

    public MemberOrganization toOrganization(final Long memberId) {
        return MemberOrganization.builder()
                .memberId(memberId)
                .officeName(officeName)
                .officeDepartment(officeDepartment)
                .officePosition(officePosition)
                .officeAddress(officeAddress)
                .officeDetailAddress(officeDetailAddress)
                .build();
    }
}
