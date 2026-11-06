package com.dgmoonlabs.cms.domain.common.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    MEMBER_JOINED("가입 환영"),
    ORDER_PAID("결제 완료"),
    ORDER_REFUNDED("환불 처리");

    private final String label;
}
