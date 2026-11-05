package com.dgmoonlabs.cms.domain.business.wallet.repository;

import com.dgmoonlabs.cms.domain.business.wallet.entity.PrepaidLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface PrepaidLedgerRepository extends JpaRepository<PrepaidLedger, Long> {
    @Query("select coalesce(sum(l.amount), 0) from PrepaidLedger l where l.memberId = :memberId")
    BigDecimal balanceOf(Long memberId);

    Page<PrepaidLedger> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);
}
