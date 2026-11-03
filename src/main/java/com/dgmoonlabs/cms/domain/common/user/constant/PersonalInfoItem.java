package com.dgmoonlabs.cms.domain.common.user.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum PersonalInfoItem {
    BIRTH_DATE("생년월일"),
    GENDER("성별"),
    EMAIL("이메일"),
    TELEPHONE("전화번호"),
    MOBILE_PHONE("휴대폰 번호"),
    ADDRESS("주소"),
    OFFICE("직장 정보");

    public static final Set<PersonalInfoItem> DEFAULTS = EnumSet.of(BIRTH_DATE, MOBILE_PHONE);

    private final String label;
}
