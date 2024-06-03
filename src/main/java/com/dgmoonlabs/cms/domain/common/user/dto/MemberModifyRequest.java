package com.dgmoonlabs.cms.domain.common.user.dto;

import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberModifyRequest {
    private Long id;
    private String username;
    private String password;
    private String passwordCheck;
    private String name;
    private String lastName;
    private String firstName;
    private LocalDate birthDate;
    private Boolean isLunarBirthDate;
    private String gender;
    private String email;
    private String telephoneName;
    private String mobilePhoneNumber;

    private String postalCode;
    private String address;
    private String detailedAddress;

    private String isMailReceptionAgreed;
    private String isSmsReceptionAgreed;
    private String isMarketingUsageAgreed;

    private String officeName;
    private String officeDepartment;
    private String officePosition;
    private String officeAddress;
    private String officeDetailAddress;
}

