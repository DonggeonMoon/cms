package com.dgmoonlabs.cms.domain.business.license.repository;

import com.dgmoonlabs.cms.domain.business.license.entity.LicenseKey;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LicenseKeyRepository extends JpaRepository<LicenseKey, Long> {
    boolean existsByProductIdAndCode(Long productId, String code);

    Page<LicenseKey> findAllByProductIdOrderByIdAsc(Long productId, Pageable pageable);

    Page<LicenseKey> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    long countByProductId(Long productId);

    long countByProductIdAndOrderIdIsNull(Long productId);

    List<LicenseKey> findAllByCodeIn(List<String> codes);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select k from LicenseKey k where k.productId = :productId and k.orderId is null order by k.id asc limit 1")
    Optional<LicenseKey> findFirstAvailable(Long productId);
}
