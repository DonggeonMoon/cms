package com.dgmoonlabs.cms.domain.business.payment.service;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.payment.MockPaymentGateway;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentRequest;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentResponse;
import com.dgmoonlabs.cms.domain.business.payment.entity.Payment;
import com.dgmoonlabs.cms.domain.business.payment.repository.PaymentRepository;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final MockPaymentGateway paymentGateway;

    @Transactional
    public PaymentResponse pay(final Long memberId, final PaymentRequest request) {
        BigDecimal expected;
        Runnable onApproved;
        if (request.targetType() == PaymentTargetType.ORDER) {
            Order order = orderRepository.findByIdAndUserTypeAndUserId(request.targetId(), UserType.MEMBER, memberId)
                    .orElseThrow(BusinessException::notFound);
            expected = order.getTotalPrice();
            onApproved = order::markPaid;
        } else {
            Reservation reservation = reservationRepository.findByIdAndUserTypeAndUserId(request.targetId(), UserType.MEMBER, memberId)
                    .orElseThrow(BusinessException::notFound);
            expected = reservation.getTotalPrice();
            onApproved = reservation::confirm;
        }

        if (expected.compareTo(request.amount()) != 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (paymentRepository.existsByTargetTypeAndTargetIdAndStatus(request.targetType(), request.targetId(),
                PaymentStatus.APPROVED)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        onApproved.run();
        Payment payment = paymentRepository.save(Payment.builder()
                .targetType(request.targetType())
                .targetId(request.targetId())
                .memberId(memberId)
                .amount(expected)
                .method(request.method())
                .status(PaymentStatus.APPROVED)
                .transactionId(paymentGateway.approve(expected, request.method()))
                .approvedAt(LocalDateTime.now())
                .build());
        return PaymentResponse.from(payment);
    }

    @Transactional
    public void refundIfPaid(final PaymentTargetType targetType, final Long targetId) {
        paymentRepository.findByTargetTypeAndTargetIdAndStatus(targetType, targetId, PaymentStatus.APPROVED)
                .ifPresent(payment -> {
                    paymentGateway.refund(payment.getTransactionId());
                    payment.cancel();
                });
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(final Pageable pageable) {
        return paymentRepository.findAllByOrderByIdDesc(pageable).map(PaymentResponse::from);
    }
}
