package com.dgmoonlabs.cms.domain.business.order.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductSaleType {
    PURCHASE("구매"),
    SUBSCRIPTION("구독"),
    DONATION("후원"),
    QUOTE("견적 문의"),
    PASS("이용권"),
    GIFT_CARD("상품권"),
    PREORDER("예약 판매"),
    FUNDING("펀딩"),
    INQUIRY("문의만");

    private final String label;
}
