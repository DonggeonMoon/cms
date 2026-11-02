package com.dgmoonlabs.cms.domain.common.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MemberModifyRequest(
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
        String officeDetailAddress
) {
}
