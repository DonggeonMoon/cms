package com.dgmoonlabs.cms.domain.common.grade.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GradeChangeReason {
    MANUAL("관리자 지정"),
    PURCHASE("구매"),
    SUBSCRIPTION("구독"),
    EXPIRED("기간 만료"),
    REFUNDED("주문 취소");

    private final String label;
}
