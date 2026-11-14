package com.dgmoonlabs.cms.domain.common.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    MEMBER_JOINED("가입 환영"),
    ORDER_PAID("결제 완료"),
    ORDER_REFUNDED("환불 처리"),
    COURSE_APPLIED("수강 신청"),
    COURSE_APPROVED("수강 승인"),
    COURSE_COMPLETED("수강 완료"),
    COURSE_QUESTION_ANSWERED("질문 답변");

    private final String label;
}
