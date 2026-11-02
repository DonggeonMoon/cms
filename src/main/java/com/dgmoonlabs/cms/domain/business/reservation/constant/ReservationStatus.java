package com.dgmoonlabs.cms.domain.business.reservation.constant;

public enum ReservationStatus {
    PENDING_PAYMENT, CONFIRMED, CANCELED;

    public boolean isCancelable() {
        return this != CANCELED;
    }
}
