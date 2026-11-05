package com.dgmoonlabs.cms.domain.business.order.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PassUnit {
    PERIOD("기간권"),
    COUNT("횟수권");

    private final String label;
}
