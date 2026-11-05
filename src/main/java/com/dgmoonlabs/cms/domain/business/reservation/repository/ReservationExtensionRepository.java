package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.business.reservation.entity.ReservationExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationExtensionRepository extends JpaRepository<ReservationExtension, Long> {
    List<ReservationExtension> findAllByReservationIdOrderByIdAsc(Long reservationId);

    List<ReservationExtension> findAllByReservationIdInOrderByIdAsc(List<Long> reservationIds);
}
