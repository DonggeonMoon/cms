package com.dgmoonlabs.cms.domain.business.payment.repository;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTargetTypeAndTargetIdAndStatus(PaymentTargetType targetType, Long targetId, PaymentStatus status);

    boolean existsByTargetTypeAndTargetIdAndStatus(PaymentTargetType targetType, Long targetId, PaymentStatus status);

    Page<Payment> findAllByOrderByIdDesc(Pageable pageable);

    Page<Payment> findByPaymentNumber(String paymentNumber, Pageable pageable);

    @Query("select p from Payment p where p.paymentNumber like concat('%', :keyword, '%') "
            + "or p.transactionId like concat('%', :keyword, '%') order by p.id desc")
    Page<Payment> search(String keyword, Pageable pageable);

    boolean existsByPaymentNumber(String paymentNumber);

    List<Payment> findAllByPaymentNumberIsNull();

    List<Payment> findAllByApprovedAtBetweenOrderByApprovedAtAsc(LocalDateTime from, LocalDateTime to);
}
