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
    Optional<ReservationItem> findByIdAndDeletedAtIsNull(Long id);

    List<ReservationItem> findAllByIsEnabledTrueAndDeletedAtIsNullOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservationItem r where r.id = :id")
    Optional<ReservationItem> findForUpdate(Long id);

    Page<ReservationItem> findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(String keyword, Pageable pageable);

    Page<ReservationItem> findAllByIsEnabledTrueAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(String keyword,
                                                                                                           Pageable pageable);
}
