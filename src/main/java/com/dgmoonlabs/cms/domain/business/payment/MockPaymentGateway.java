package com.dgmoonlabs.cms.domain.business.payment;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class MockPaymentGateway {
    public String approve(final BigDecimal amount, final PaymentMethod method) {
        String transactionId = "MOCK-" + UUID.randomUUID();
        log.info("[mock-pg] approved {} {} -> {}", method, amount, transactionId);
        return transactionId;
    }

    public void refund(final String transactionId, final BigDecimal amount) {
        log.info("[mock-pg] refunded {} {}", amount, transactionId);
    }
}
