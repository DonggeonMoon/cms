package com.dgmoonlabs.cms.domain.business.progress.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 진행 단계·고객 메시지를 붙일 수 있는 대상. 주문 제작·AS·의뢰·채용에서 늘어날 수 있다. */
@Getter
@RequiredArgsConstructor
public enum ProgressTargetType {
    ORDER("주문"),
    REPAIR("수리·AS 접수"),
    APPLICATION("신청·지원");

    private final String label;
}
