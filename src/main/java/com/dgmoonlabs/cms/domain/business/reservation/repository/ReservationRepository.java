package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdAndUserTypeAndUserId(Long id, UserType userType, Long userId);

    Page<Reservation> findAllByUserTypeAndUserIdOrderByIdDesc(UserType userType, Long userId, Pageable pageable);

    @Query("select r from Reservation r where (:programId is null or r.programId = :programId) "
            + "and (:status is null or r.status = :status) order by r.id desc")
    Page<Reservation> search(Long programId, ReservationStatus status, Pageable pageable);

    boolean existsByReservationNumber(String reservationNumber);

    boolean existsByUserTypeAndUserIdAndProgramIdAndStatus(UserType userType, Long userId, Long programId, ReservationStatus status);

    long countByStatus(ReservationStatus status);
}
