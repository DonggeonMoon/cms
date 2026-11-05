package com.dgmoonlabs.cms.domain.business.order.constant;

public enum FulfillmentType {
    DELIVERY, PICKUP, DINE_IN, LOCAL_DELIVERY;

    /** 매장에서 직접 준비해 내보내는 주문인지(포장·매장 식사·배달), 택배로 보내는 주문인지. */
    public boolean isStore() {
        return this != DELIVERY;
    }
}
