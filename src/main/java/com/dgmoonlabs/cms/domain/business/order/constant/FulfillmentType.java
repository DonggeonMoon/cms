package com.dgmoonlabs.cms.domain.business.order.constant;

public enum FulfillmentType {
    DELIVERY, PICKUP, DINE_IN;

    public boolean isStore() {
        return this != DELIVERY;
    }
}
