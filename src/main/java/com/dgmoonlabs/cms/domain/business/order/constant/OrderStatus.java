package com.dgmoonlabs.cms.domain.business.order.constant;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    QUOTE_REQUESTED, QUOTED, PENDING_PAYMENT, PAID, SHIPPING, DELIVERED, PREPARING, READY, COMPLETED, CANCELED;

    /** 배송 주문은 결제 완료 → 배송 중 → 배송 완료, 매장(포장·매장 식사) 주문은 결제 완료 → 준비 중 → 준비 완료 → 전달 완료. */
    private static final Map<OrderStatus, Set<OrderStatus>> ADMIN_TRANSITIONS = Map.of(
            PAID, Set.of(SHIPPING, PREPARING),
            SHIPPING, Set.of(DELIVERED),
            PREPARING, Set.of(READY),
            READY, Set.of(COMPLETED)
    );
    private static final Set<OrderStatus> STORE_ONLY = Set.of(PREPARING, READY, COMPLETED);
    private static final Set<OrderStatus> DELIVERY_ONLY = Set.of(SHIPPING, DELIVERED);

    public boolean canMoveTo(final OrderStatus next, final boolean storeOrder) {
        return ADMIN_TRANSITIONS.getOrDefault(this, Set.of()).contains(next)
                && !(storeOrder ? DELIVERY_ONLY : STORE_ONLY).contains(next);
    }

    public boolean isCancelable() {
        return this == QUOTE_REQUESTED || this == QUOTED || this == PENDING_PAYMENT || this == PAID;
    }

    /** 결제가 끝나고 물건을 받은(또는 받는 중인) 상태만 반품·교환을 신청할 수 있다. */
    public boolean isReturnable() {
        return this == PAID || this == SHIPPING || this == PREPARING || this == READY || this == DELIVERED || this == COMPLETED;
    }
}
