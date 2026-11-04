package com.dgmoonlabs.cms.domain.common.point.repository;

import com.dgmoonlabs.cms.domain.common.point.entity.PointLedger;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {
    Page<PointLedger> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    @Query("""
            select coalesce(sum(l.remaining), 0) from PointLedger l
            where l.memberId = :memberId and l.remaining > 0 and (l.expiresAt is null or l.expiresAt > :now)
            """)
    long balanceOf(Long memberId, LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l from PointLedger l
            where l.memberId = :memberId and l.remaining > 0 and (l.expiresAt is null or l.expiresAt > :now)
            order by case when l.expiresAt is null then 1 else 0 end, l.expiresAt, l.id
            """)
    List<PointLedger> findUsableForUpdate(Long memberId, LocalDateTime now);

    @Query("""
            select l from PointLedger l
            where l.memberId = :memberId and l.remaining > 0 and l.expiresAt > :now and l.expiresAt <= :by
            """)
    List<PointLedger> findExpiringBetween(Long memberId, LocalDateTime now, LocalDateTime by);

    @Query("select l from PointLedger l where l.remaining > 0 and l.expiresAt <= :now")
    List<PointLedger> findExpired(LocalDateTime now);
}
