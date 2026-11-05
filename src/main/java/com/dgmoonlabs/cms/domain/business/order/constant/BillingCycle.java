package com.dgmoonlabs.cms.domain.business.order.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public enum BillingCycle {
    MONTHLY("매월"),
    YEARLY("매년");

    private final String label;

    public LocalDate next(final LocalDate from) {
        return this == MONTHLY ? from.plusMonths(1) : from.plusYears(1);
    }

    public int monthsPerCycle() {
        return this == MONTHLY ? 1 : 12;
    }
}
