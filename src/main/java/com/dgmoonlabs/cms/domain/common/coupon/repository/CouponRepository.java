package com.dgmoonlabs.cms.domain.common.coupon.repository;

import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueType;
import com.dgmoonlabs.cms.domain.common.coupon.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByDeletedAtIsNullOrderByIdDesc();

    List<Coupon> findAllByIssueTypeAndDeletedAtIsNull(CouponIssueType issueType);

    Optional<Coupon> findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.id = :id")
    Optional<Coupon> findForUpdate(Long id);
}
