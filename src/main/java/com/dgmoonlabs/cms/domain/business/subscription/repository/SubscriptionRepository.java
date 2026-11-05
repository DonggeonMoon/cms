package com.dgmoonlabs.cms.domain.business.subscription.repository;

import com.dgmoonlabs.cms.domain.business.subscription.constant.SubscriptionStatus;
import com.dgmoonlabs.cms.domain.business.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsByFirstOrderId(Long firstOrderId);

    Optional<Subscription> findByIdAndMemberId(Long id, Long memberId);

    Page<Subscription> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    Page<Subscription> findAllByOrderByIdDesc(Pageable pageable);

    List<Subscription> findAllByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus status, LocalDate date);

    List<Subscription> findAllByStatusAndResumeOnLessThanEqual(SubscriptionStatus status, LocalDate date);
}
