package com.dgmoonlabs.cms.domain.business.payment.service;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.payment.PaymentGateway;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentRequest;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentResponse;
import com.dgmoonlabs.cms.domain.business.payment.entity.Payment;
import com.dgmoonlabs.cms.domain.business.payment.repository.PaymentRepository;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.entity.ReservationExtension;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationExtensionRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.domain.business.BusinessNumbers;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.service.RegexService;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import com.dgmoonlabs.cms.domain.business.wallet.constant.LedgerType;
import com.dgmoonlabs.cms.domain.business.wallet.service.PrepaidService;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationExtensionRepository extensionRepository;
    private final PaymentGateway paymentGateway;
    private final RegexService regexService;
    private final ApplicationEventPublisher events;
    private final PrepaidService prepaidService;

    @Transactional
    public PaymentResponse pay(final Long memberId, final PaymentRequest request) {
        BigDecimal expected;
        Runnable onApproved;
        if (request.targetType() == PaymentTargetType.ORDER) {
            Order order = orderRepository.findByIdAndUserTypeAndUserId(request.targetId(), UserType.MEMBER, memberId)
                    .orElseThrow(BusinessException::notFound);
            expected = order.getTotalPrice();
            onApproved = order::markPaid;
        } else if (request.targetType() == PaymentTargetType.RESERVATION_EXTENSION) {
            ReservationExtension extension = extensionRepository.findById(request.targetId()).orElseThrow(BusinessException::notFound);
            Reservation reservation = reservationRepository.findByIdAndUserTypeAndUserId(extension.getReservationId(), UserType.MEMBER, memberId)
                    .orElseThrow(BusinessException::notFound);
            expected = extension.getFee();
            onApproved = () -> {
                extension.markPaid();
                reservation.extensionPaid(extension.getId());
            };
        } else {
            Reservation reservation = reservationRepository.findByIdAndUserTypeAndUserId(request.targetId(), UserType.MEMBER, memberId)
                    .orElseThrow(BusinessException::notFound);
            expected = reservation.getTotalPrice();
            onApproved = reservation::confirm;
        }

        if (expected == null) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        if (expected.compareTo(request.amount()) != 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (paymentRepository.existsByTargetTypeAndTargetIdAndStatus(request.targetType(), request.targetId(),
                PaymentStatus.APPROVED)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        if (request.method() == PaymentMethod.PREPAID) {
            prepaidService.use(memberId, expected, LedgerType.PAYMENT, request.targetId(), "결제");
        }
        onApproved.run();
        Payment payment = paymentRepository.save(Payment.builder()
                .paymentNumber(nextNumber(LocalDateTime.now()))
                .targetType(request.targetType())
                .targetId(request.targetId())
                .memberId(memberId)
                .amount(expected)
                .method(request.method())
                .status(PaymentStatus.APPROVED)
                .transactionId(paymentGateway.approve(expected, request.method()))
                .approvedAt(LocalDateTime.now())
                .build());
        if (request.targetType() == PaymentTargetType.ORDER) {
            events.publishEvent(new OrderPaidEvent(request.targetId()));
        }
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse recordAutomaticPayment(final Long memberId, final Order order) {
        Payment payment = paymentRepository.save(Payment.builder()
                .paymentNumber(nextNumber(LocalDateTime.now()))
                .targetType(PaymentTargetType.ORDER)
                .targetId(order.getId())
                .memberId(memberId)
                .amount(order.getTotalPrice())
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.APPROVED)
                .transactionId(paymentGateway.approve(order.getTotalPrice(), PaymentMethod.CARD))
                .approvedAt(LocalDateTime.now())
                .build());
        return PaymentResponse.from(payment);
    }

    @Transactional
    public void refundIfPaid(final PaymentTargetType targetType, final Long targetId) {
        paymentRepository.findByTargetTypeAndTargetIdAndStatus(targetType, targetId, PaymentStatus.APPROVED)
                .ifPresent(payment -> {
                    BigDecimal refund = payment.refundable();
                    if (payment.getMethod() == PaymentMethod.PREPAID) {
                        prepaidService.credit(payment.getMemberId(), refund, LedgerType.REFUND, payment.getTargetId(), "결제 환불");
                    } else {
                        paymentGateway.refund(payment.getTransactionId(), refund);
                    }
                    payment.cancel();
                });
    }

    @Transactional
    public void refundPartially(final PaymentTargetType targetType, final Long targetId, final BigDecimal amount) {
        if (amount.signum() <= 0) {
            return;
        }
        paymentRepository.findByTargetTypeAndTargetIdAndStatus(targetType, targetId, PaymentStatus.APPROVED)
                .ifPresent(payment -> {
                    BigDecimal refund = amount.min(payment.refundable());
                    if (payment.getMethod() == PaymentMethod.PREPAID) {
                        prepaidService.credit(payment.getMemberId(), refund, LedgerType.REFUND, payment.getTargetId(), "결제 환불");
                    } else {
                        paymentGateway.refund(payment.getTransactionId(), refund);
                    }
                    payment.refund(refund);
                });
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(final String keyword, final Pageable pageable) {
        String text = keyword == null ? "" : keyword.trim();
        if (text.isEmpty()) {
            return paymentRepository.findAllByOrderByIdDesc(pageable).map(PaymentResponse::from);
        }
        if (regexService.matches(RegexTarget.PAYMENT_NUMBER, text)) {
            return paymentRepository.findByPaymentNumber(text, pageable).map(PaymentResponse::from);
        }
        return paymentRepository.search(text, pageable).map(PaymentResponse::from);
    }

    @Transactional
    public int assignMissingNumbers() {
        List<Payment> payments = paymentRepository.findAllByPaymentNumberIsNull();
        for (Payment payment : payments) {
            LocalDateTime at = payment.getApprovedAt() != null ? payment.getApprovedAt() : payment.getCreatedAt();
            payment.assignNumber(nextNumber(at != null ? at : LocalDateTime.now()));
            paymentRepository.flush();
        }
        return payments.size();
    }

    private String nextNumber(final LocalDateTime at) {
        return regexService.requireNumberFormat(RegexTarget.PAYMENT_NUMBER,
                BusinessNumbers.next("P", at, paymentRepository::existsByPaymentNumber));
    }
}
