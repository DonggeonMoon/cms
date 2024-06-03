package com.dgmoonlabs.cms.domain.common.user.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class UsernameCheckRequest {
    private String username;
    private String name;
    private String birthday;
    private String email;
    private String phoneNumber;
}
