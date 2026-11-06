package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.business.reservation.entity.reservationitem.ReservationItemSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationItemSessionRepository extends JpaRepository<ReservationItemSession, Long> {
    List<ReservationItemSession> findAllByReservationItemIdAndDeletedAtIsNullOrderByStartAtAsc(Long reservationItemId);

    boolean existsByReservationItemIdAndDeletedAtIsNull(Long reservationItemId);

    Optional<ReservationItemSession> findByIdAndReservationItemIdAndDeletedAtIsNull(Long id, Long reservationItemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ReservationItemSession s where s.id = :id")
    Optional<ReservationItemSession> findForUpdate(Long id);
}
