package com.dgmoonlabs.cms.domain.common.coupon.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponIssueType {
    DOWNLOAD("다운로드"),
    SIGNUP("가입 시 자동 지급"),
    GRADE("등급 달성 시 자동 지급"),
    MANUAL("관리자 지급");

    private final String label;
}
