package com.dgmoonlabs.cms.domain.business.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class PaymentNumberBackfill implements ApplicationRunner {
    private final PaymentService paymentService;

    @Override
    public void run(final ApplicationArguments args) {
        int assigned = paymentService.assignMissingNumbers();
        if (assigned > 0) {
            log.info("Assigned payment numbers to {} existing payments", assigned);
        }
    }
}
