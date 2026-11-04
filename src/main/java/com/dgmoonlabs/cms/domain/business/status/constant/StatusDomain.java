package com.dgmoonlabs.cms.domain.business.status.constant;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum StatusDomain {
    ORDER, RESERVATION;

    /** 기본 상태와 처음 이름. 기본 상태는 결제·취소 같은 처리와 묶여 있어 이름만 바꿀 수 있다. */
    public Map<String, String> systemStatuses() {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (this == ORDER) {
            Map<OrderStatus, String> labels = Map.of(OrderStatus.QUOTE_REQUESTED, "견적 요청", OrderStatus.QUOTED, "견적 회신",
                    OrderStatus.PENDING_PAYMENT, "결제 대기", OrderStatus.PAID, "결제 완료", OrderStatus.SHIPPING, "배송 중",
                    OrderStatus.DELIVERED, "배송 완료", OrderStatus.CANCELED, "취소");
            Arrays.stream(OrderStatus.values()).forEach(status -> statuses.put(status.name(), labels.get(status)));
        } else {
            Map<ReservationStatus, String> labels = Map.of(ReservationStatus.PENDING_PAYMENT, "결제 대기",
                    ReservationStatus.CONFIRMED, "확정", ReservationStatus.CANCELED, "취소");
            Arrays.stream(ReservationStatus.values()).forEach(status -> statuses.put(status.name(), labels.get(status)));
        }
        return statuses;
    }
}
