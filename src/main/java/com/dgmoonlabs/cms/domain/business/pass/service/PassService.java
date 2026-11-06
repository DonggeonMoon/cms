package com.dgmoonlabs.cms.domain.business.pass.service;

import com.dgmoonlabs.cms.domain.business.order.constant.PassUnit;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.pass.constant.PassStatus;
import com.dgmoonlabs.cms.domain.business.pass.dto.PassDtos.PassResponse;
import com.dgmoonlabs.cms.domain.business.pass.entity.MemberPass;
import com.dgmoonlabs.cms.domain.business.pass.repository.MemberPassRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PassService {
    private final MemberPassRepository passRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @EventListener
    @Transactional
    public void issueOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getSaleType() != ProductSaleType.PASS || order.getUserType() != UserType.MEMBER
                || passRepository.existsByFirstOrderId(order.getId())) {
            return;
        }
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product == null || product.getPassUnit() == null) {
            return;
        }
        passRepository.save(MemberPass.builder()
                .memberId(order.getUserId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .unit(product.getPassUnit())
                .reservationItemId(product.getPassReservationItemId())
                .totalCount(product.getPassUnit() == PassUnit.COUNT ? product.getPassCount() * order.getQuantity() : null)
                .remainingCount(product.getPassUnit() == PassUnit.COUNT ? product.getPassCount() * order.getQuantity() : null)
                .expiresAt(product.getPassUnit() == PassUnit.PERIOD ? LocalDateTime.now().plusDays(product.getPassDays()) : null)
                .firstOrderId(order.getId())
                .status(PassStatus.ACTIVE)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<PassResponse> getMine(final Long memberId, final Pageable pageable) {
        return passRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable).map(PassResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PassResponse> search(final Long memberId, final Pageable pageable) {
        return passRepository.search(memberId, pageable).map(PassResponse::from);
    }

    @Transactional(readOnly = true)
    public List<PassResponse> usableFor(final Long memberId, final Long reservationItemId) {
        LocalDateTime now = LocalDateTime.now();
        return passRepository.findAllByMemberIdAndReservationItemIdAndStatusOrderByIdAsc(memberId, reservationItemId, PassStatus.ACTIVE).stream()
                .filter(pass -> pass.isUsableFor(reservationItemId, now))
                .map(PassResponse::from)
                .toList();
    }

    @Transactional
    public MemberPass consumeForMember(final Long memberId, final Long passId, final Long reservationItemId, final int quantity) {
        MemberPass pass = passRepository.findForUpdate(passId).filter(found -> found.getMemberId().equals(memberId))
                .orElseThrow(BusinessException::notFound);
        if (!pass.isUsableFor(reservationItemId, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PASS_NOT_USABLE);
        }
        pass.consume(quantity);
        return pass;
    }

    @Transactional
    public void restoreUsage(final Long passId, final int quantity) {
        passRepository.findForUpdate(passId).ifPresent(pass -> pass.restore(quantity));
    }

    @Transactional
    public void cancel(final Long id) {
        passRepository.findById(id).orElseThrow(BusinessException::notFound).cancel();
    }
}
