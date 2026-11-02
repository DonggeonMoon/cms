package com.dgmoonlabs.cms.domain.business.payment;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

// stand-in until a real PG is integrated: every request is approved and refunds always succeed
@Slf4j
@Component
public class MockPaymentGateway {
    public String approve(final BigDecimal amount, final PaymentMethod method) {
        String transactionId = "MOCK-" + UUID.randomUUID();
        log.info("[mock-pg] approved {} {} -> {}", method, amount, transactionId);
        return transactionId;
    }

    public void refund(final String transactionId) {
        log.info("[mock-pg] refunded {}", transactionId);
    }
}
