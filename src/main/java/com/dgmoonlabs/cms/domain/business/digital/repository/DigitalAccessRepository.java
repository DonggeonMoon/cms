package com.dgmoonlabs.cms.domain.business.digital.repository;

import com.dgmoonlabs.cms.domain.business.digital.entity.DigitalAccess;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DigitalAccessRepository extends JpaRepository<DigitalAccess, Long> {
    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    Page<DigitalAccess> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    List<DigitalAccess> findAllByOrderIdAndProductIdIn(Long orderId, Collection<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from DigitalAccess a where a.id = :id and a.memberId = :memberId")
    Optional<DigitalAccess> findForDownload(Long id, Long memberId);
}
