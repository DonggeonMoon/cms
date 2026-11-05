package com.dgmoonlabs.cms.domain.business.order.service;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 펀딩은 마감일이 지나면 목표 금액을 채웠는지 본다. 못 채웠으면 결제된 주문을 모두 취소해 환불한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundingService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(cron = "${cms.funding.resolve-cron:0 20 3 * * *}")
    public void resolveDueOnSchedule() {
        int failed = resolveDue(LocalDateTime.now());
        if (failed > 0) {
            log.info("Refunded {} failed funding campaigns", failed);
        }
    }

    @Transactional
    public int resolveDue(final LocalDateTime now) {
        int failedCampaigns = 0;
        for (Product product : productRepository.findAllBySaleTypeAndFundingDeadlineLessThanEqualAndDeletedAtIsNull(
                ProductSaleType.FUNDING, now)) {
            if (hasReachedGoal(product)) {
                continue;
            }
            var paidOrders = orderRepository.findAllByProductIdAndSaleTypeAndStatus(product.getId(), ProductSaleType.FUNDING,
                    OrderStatus.PAID);
            if (paidOrders.isEmpty()) {
                continue;
            }
            for (Order order : paidOrders) {
                orderService.changeStatus(order.getId(), OrderStatus.CANCELED);
            }
            failedCampaigns++;
        }
        return failedCampaigns;
    }

    private boolean hasReachedGoal(final Product product) {
        BigDecimal raised = orderRepository.sumPaidFunding(product.getId());
        return product.getGoalAmount() != null && raised.compareTo(product.getGoalAmount()) >= 0;
    }
}
