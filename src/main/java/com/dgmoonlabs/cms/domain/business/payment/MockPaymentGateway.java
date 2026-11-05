package com.dgmoonlabs.cms.domain.business.payment;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "cms.payment", name = "gateway-mode", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public String approve(final BigDecimal amount, final PaymentMethod method) {
        String transactionId = "MOCK-" + UUID.randomUUID();
        log.info("[mock-pg] approved {} {} -> {}", method, amount, transactionId);
        return transactionId;
    }

    @Override
    public void refund(final String transactionId, final BigDecimal amount) {
        log.info("[mock-pg] refunded {} {}", amount, transactionId);
    }
}
