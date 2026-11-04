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
}
