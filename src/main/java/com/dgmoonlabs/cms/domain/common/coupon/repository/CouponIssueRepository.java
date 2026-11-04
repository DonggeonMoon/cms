package com.dgmoonlabs.cms.domain.common.coupon.repository;

import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueStatus;
import com.dgmoonlabs.cms.domain.common.coupon.entity.CouponIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {
    List<CouponIssue> findAllByMemberIdOrderByIdDesc(Long memberId);

    Optional<CouponIssue> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByCouponIdAndMemberId(Long couponId, Long memberId);

    Page<CouponIssue> findAllByCouponIdOrderByIdDesc(Long couponId, Pageable pageable);

    @Query("select i from CouponIssue i where i.status = :status and i.expiresAt <= :now")
    List<CouponIssue> findExpired(CouponIssueStatus status, LocalDateTime now);
}
