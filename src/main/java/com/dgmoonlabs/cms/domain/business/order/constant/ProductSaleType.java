package com.dgmoonlabs.cms.domain.business.order.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductSaleType {
    PURCHASE("구매"),
    SUBSCRIPTION("구독"),
    DONATION("후원"),
    QUOTE("견적 문의");

    private final String label;
}
