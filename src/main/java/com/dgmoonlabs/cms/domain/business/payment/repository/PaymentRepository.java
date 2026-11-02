package com.dgmoonlabs.cms.domain.business.payment.repository;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTargetTypeAndTargetIdAndStatus(PaymentTargetType targetType, Long targetId, PaymentStatus status);

    boolean existsByTargetTypeAndTargetIdAndStatus(PaymentTargetType targetType, Long targetId, PaymentStatus status);

    Page<Payment> findAllByOrderByIdDesc(Pageable pageable);
}
