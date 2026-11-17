package com.dgmoonlabs.cms.domain.business.order.constant;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    QUOTE_REQUESTED, QUOTED, PENDING_PAYMENT, PAID, SHIPPING, DELIVERED, CANCELED;

    private static final Map<OrderStatus, Set<OrderStatus>> ADMIN_TRANSITIONS = Map.of(
            PAID, Set.of(SHIPPING),
            SHIPPING, Set.of(DELIVERED)
    );

    public boolean canMoveTo(final OrderStatus next) {
        return ADMIN_TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }

    public boolean isCancelable() {
        return this == QUOTE_REQUESTED || this == QUOTED || this == PENDING_PAYMENT || this == PAID;
    }

    /** 결제가 끝나고 물건을 받은(또는 받는 중인) 상태만 반품·교환을 신청할 수 있다. */
    public boolean isReturnable() {
        return this == PAID || this == SHIPPING || this == DELIVERED;
    }
}
