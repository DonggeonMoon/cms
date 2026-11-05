package com.dgmoonlabs.cms.domain.business.payment;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;

import java.math.BigDecimal;

/** 실제 PG 연동 시 구현체를 빈으로 등록하고 cms.payment.gateway-mode=real 로 설정한다. */
public interface PaymentGateway {
    String approve(BigDecimal amount, PaymentMethod method);

    void refund(String transactionId, BigDecimal amount);
}
