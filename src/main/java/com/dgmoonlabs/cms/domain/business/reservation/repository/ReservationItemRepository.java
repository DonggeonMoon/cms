package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.business.reservation.entity.reservationitem.ReservationItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {
    Optional<ReservationItem> findByIdAndSiteIdAndDeletedAtIsNull(Long id, Long siteId);

    /** 이미 만들어진 예약이 가리키는 상품을 조회할 때 쓴다 - 사이트 전환과 무관하게 항상 찾아야 하므로 siteId 필터를 두지 않는다. */
    Optional<ReservationItem> findByIdAndDeletedAtIsNull(Long id);

    List<ReservationItem> findAllByIsEnabledTrueAndSiteIdAndDeletedAtIsNullOrderByIdAsc(Long siteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservationItem r where r.id = :id")
    Optional<ReservationItem> findForUpdate(Long id);

    Page<ReservationItem> findAllBySiteIdAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(Long siteId, String keyword, Pageable pageable);

    Page<ReservationItem> findAllByIsEnabledTrueAndSiteIdAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(Long siteId, String keyword,
                                                                                                           Pageable pageable);
}
