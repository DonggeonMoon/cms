package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAddress;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAgreement;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberOrganization;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String username,
        String nickname,
        String name,
        String lastName,
        String firstName,
        LocalDate birthDate,
        String gender,
        String email,
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
        int loginTryCount,
        LocalDateTime lastLoginDateTime,
        LocalDateTime createdAt
) {
    public static MemberResponse of(final Member member, final MemberAddress address,
                                    final MemberAgreement agreement, final MemberOrganization organization) {
        return new MemberResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getName(),
                member.getLastName(),
                member.getFirstName(),
                member.getBirthDate(),
                member.getGender(),
                member.getEmail(),
                member.getTelephoneNumber(),
                member.getMobilePhoneNumber(),
                address == null ? null : address.getPostalCode(),
                address == null ? null : address.getAddress(),
                address == null ? null : address.getDetailAddress(),
                agreement != null && agreement.isMailReceptionAgreed(),
                agreement != null && agreement.isSmsReceptionAgreed(),
                agreement != null && agreement.isMarketingUsageAgreed(),
                organization == null ? null : organization.getOfficeName(),
                organization == null ? null : organization.getOfficeDepartment(),
                organization == null ? null : organization.getOfficePosition(),
                organization == null ? null : organization.getOfficeAddress(),
                organization == null ? null : organization.getOfficeDetailAddress(),
                member.getLoginTryCount(),
                member.getLastLoginDateTime(),
                member.getCreatedAt()
        );
    }
}
