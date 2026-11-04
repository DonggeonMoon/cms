package com.dgmoonlabs.cms.domain.business.subscription.service;

import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.business.subscription.constant.SubscriptionStatus;
import com.dgmoonlabs.cms.domain.business.subscription.dto.SubscriptionResponse;
import com.dgmoonlabs.cms.domain.business.subscription.entity.Subscription;
import com.dgmoonlabs.cms.domain.business.subscription.repository.SubscriptionRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @EventListener
    public void startOnFirstPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getSaleType() != ProductSaleType.SUBSCRIPTION || order.getSubscriptionId() != null
                || order.getUserType() != UserType.MEMBER || subscriptionRepository.existsByFirstOrderId(order.getId())) {
            return;
        }
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product == null || product.getBillingCycle() == null) {
            return;
        }
        Subscription subscription = subscriptionRepository.save(Subscription.builder()
                .memberId(order.getUserId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .firstOrderId(order.getId())
                .price(order.getTotalPrice())
                .billingCycle(product.getBillingCycle())
                .nextBillingDate(product.getBillingCycle().next(LocalDate.now()))
                .build());
        order.linkSubscription(subscription.getId());
    }

    @Scheduled(cron = "${cms.subscription.renew-cron:0 0 3 * * *}")
    public void renewDueOnSchedule() {
        int renewed = renewDue(LocalDate.now());
        if (renewed > 0) {
            log.info("Renewed {} subscriptions", renewed);
        }
    }

    @Transactional
    public int renewDue(final LocalDate today) {
        int renewed = 0;
        for (Subscription subscription : subscriptionRepository.findAllByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus.ACTIVE, today)) {
            try {
                Order order = orderService.createRenewal(subscription.getMemberId(), subscription.getId(),
                        subscription.getFirstOrderId(), subscription.getPrice());
                paymentService.recordAutomaticPayment(subscription.getMemberId(), order);
                subscription.billed();
                renewed++;
            } catch (BusinessException e) {
                log.warn("Subscription {} could not be renewed: {}", subscription.getId(), e.getErrorCode());
            }
        }
        return renewed;
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getMine(final Long memberId, final Pageable pageable) {
        return subscriptionRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable).map(SubscriptionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> search(final Pageable pageable) {
        return subscriptionRepository.findAllByOrderByIdDesc(pageable).map(SubscriptionResponse::from);
    }

    @Transactional
    public SubscriptionResponse cancelMine(final Long memberId, final Long id) {
        Subscription subscription = subscriptionRepository.findByIdAndMemberId(id, memberId).orElseThrow(BusinessException::notFound);
        subscription.cancel();
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse cancel(final Long id) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(BusinessException::notFound);
        subscription.cancel();
        return SubscriptionResponse.from(subscription);
    }
}
