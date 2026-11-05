package com.dgmoonlabs.cms.domain.business.subscription.service;

import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.ProductSummary;
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
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final GradeService gradeService;

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
                .commitmentMonths(product.getCommitmentMonths())
                .commitmentEndDate(product.getCommitmentMonths() == null ? null : LocalDate.now().plusMonths(product.getCommitmentMonths()))
                .earlyTerminationFeeRate(product.getEarlyTerminationFeeRate())
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

    /** 다시 시작할 날이 된 일시정지 구독을 먼저 재개한 뒤, 결제일이 된 구독을 결제한다. */
    @Transactional
    public int renewDue(final LocalDate today) {
        for (Subscription paused : subscriptionRepository.findAllByStatusAndResumeOnLessThanEqual(SubscriptionStatus.PAUSED, today)) {
            paused.resume(today);
        }
        int renewed = 0;
        for (Subscription subscription : subscriptionRepository.findAllByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus.ACTIVE, today)) {
            try {
                boolean change = subscription.hasPendingChange();
                Order order = orderService.createRenewal(subscription.getMemberId(), subscription.getId(),
                        subscription.getFirstOrderId(),
                        change ? subscription.getPendingProductId() : subscription.getProductId(),
                        change ? subscription.getPendingProductName() : subscription.getProductName(),
                        change ? subscription.getPendingPrice() : subscription.getPrice());
                paymentService.recordAutomaticPayment(subscription.getMemberId(), order);
                subscription.applyPlanChange();
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
        Subscription subscription = findMine(memberId, id);
        chargeEarlyTerminationFeeIfAny(subscription);
        subscription.cancel();
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse cancel(final Long id) {
        Subscription subscription = find(id);
        chargeEarlyTerminationFeeIfAny(subscription);
        subscription.cancel();
        return SubscriptionResponse.from(subscription);
    }

    /** 약정 기간이 남았으면 위약금을 결제 완료 주문으로 바로 청구한다. */
    private void chargeEarlyTerminationFeeIfAny(final Subscription subscription) {
        BigDecimal fee = subscription.earlyTerminationFee(LocalDate.now());
        if (fee.signum() > 0) {
            Order penaltyOrder = orderService.createSubscriptionPenalty(subscription.getMemberId(), subscription.getId(),
                    subscription.getProductId(), subscription.getProductName(), fee);
            paymentService.recordAutomaticPayment(subscription.getMemberId(), penaltyOrder);
        }
    }

    @Transactional
    public SubscriptionResponse pauseMine(final Long memberId, final Long id, final LocalDate resumeOn) {
        return pause(findMine(memberId, id), resumeOn);
    }

    @Transactional
    public SubscriptionResponse pause(final Long id, final LocalDate resumeOn) {
        return pause(find(id), resumeOn);
    }

    @Transactional
    public SubscriptionResponse resumeMine(final Long memberId, final Long id) {
        return resume(findMine(memberId, id));
    }

    @Transactional
    public SubscriptionResponse resume(final Long id) {
        return resume(find(id));
    }

    /** 바꿀 수 있는 구독 상품: 판매 중인 다른 구독 상품. */
    @Transactional(readOnly = true)
    public List<ProductSummary> getPlanOptions(final Long memberId, final Long id) {
        Subscription subscription = findMine(memberId, id);
        return productRepository.findAllByIsEnabledTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                .filter(product -> product.getSaleType() == ProductSaleType.SUBSCRIPTION && product.getBillingCycle() != null
                        && !product.getId().equals(subscription.getProductId()))
                .map(ProductSummary::from)
                .toList();
    }

    /** 바꾼 상품은 다음 결제일부터 적용한다. 금액은 새 상품 가격 × 처음 구독한 수량. */
    @Transactional
    public SubscriptionResponse changePlanMine(final Long memberId, final Long id, final Long productId) {
        Subscription subscription = findMine(memberId, id);
        if (productId.equals(subscription.getProductId())) {
            subscription.cancelPlanChange();
            return SubscriptionResponse.from(subscription);
        }
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .filter(found -> found.isEnabled() && found.getSaleType() == ProductSaleType.SUBSCRIPTION && found.getBillingCycle() != null)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_PLAN));
        gradeService.require(product.getAccessGradeId());
        int quantity = orderRepository.findById(subscription.getFirstOrderId()).map(Order::getQuantity).orElse(1);
        subscription.schedulePlanChange(product.getId(), product.getName(),
                product.getPrice().multiply(BigDecimal.valueOf(quantity)), product.getBillingCycle());
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse cancelPlanChangeMine(final Long memberId, final Long id) {
        Subscription subscription = findMine(memberId, id);
        subscription.cancelPlanChange();
        return SubscriptionResponse.from(subscription);
    }

    private SubscriptionResponse pause(final Subscription subscription, final LocalDate resumeOn) {
        subscription.pause(LocalDate.now(), resumeOn);
        return SubscriptionResponse.from(subscription);
    }

    private SubscriptionResponse resume(final Subscription subscription) {
        subscription.resume(LocalDate.now());
        return SubscriptionResponse.from(subscription);
    }

    private Subscription findMine(final Long memberId, final Long id) {
        return subscriptionRepository.findByIdAndMemberId(id, memberId).orElseThrow(BusinessException::notFound);
    }

    private Subscription find(final Long id) {
        return subscriptionRepository.findById(id).orElseThrow(BusinessException::notFound);
    }
}
