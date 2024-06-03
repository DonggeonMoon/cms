package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberJoinRequest {
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

    public Member toEntity() {
        return Member.builder()
                .username(username)
                .password(password)
                .name(name)
                .lastName(lastName)
                .firstName(firstName)
                .birthDate(birthDate)
                .isLunarBirthDate(isLunarBirthDate)
                .gender(gender)
                .email(email)
                .telephoneNumber(telephoneName)
                .mobilePhoneNumber(mobilePhoneNumber)
                .build();
    }
}

