package com.dgmoonlabs.cms.domain.common.point.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    EARN("구매 적립"),
    SIGNUP("가입 적립"),
    REFERRAL("추천인 적립"),
    ADMIN("관리자 조정"),
    USE("사용"),
    RESTORE("사용 취소"),
    REVOKE("적립 취소"),
    EXPIRE("소멸");

    private final String label;
}
