package com.dgmoonlabs.cms.domain.business.payment.service;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.entity.Payment;
import com.dgmoonlabs.cms.domain.business.payment.repository.PaymentRepository;
import com.dgmoonlabs.cms.domain.business.reservation.service.ReservationService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자가 결제 내역에서 결제 하나를 직접 취소할 때 쓴다. 결제만 취소하면 주문/예약 상태가 결제와
 * 어긋나므로, 대상 종류에 맞는 기존 취소 로직(주문 취소, 예약 취소)을 그대로 호출해 상태를 맞춘다.
 * {@link OrderService}, {@link ReservationService}가 이미 {@link PaymentService}에 의존하므로,
 * 순환 의존을 피하기 위해 이 조정 로직은 PaymentService가 아닌 별도 서비스에 둔다.
 */
@Service
@RequiredArgsConstructor
public class PaymentCancellationService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;

    @Transactional
    public void cancel(final Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(BusinessException::notFound);
        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        switch (payment.getTargetType()) {
            case ORDER -> orderService.changeStatus(payment.getTargetId(), OrderStatus.CANCELED);
            case RESERVATION -> reservationService.cancelReservation(payment.getTargetId());
            case RESERVATION_EXTENSION -> paymentService.refundIfPaid(PaymentTargetType.RESERVATION_EXTENSION, payment.getTargetId());
        }
    }
}
