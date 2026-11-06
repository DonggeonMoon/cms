package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdAndUserTypeAndUserId(Long id, UserType userType, Long userId);

    Page<Reservation> findAllByUserTypeAndUserIdOrderByIdDesc(UserType userType, Long userId, Pageable pageable);

    @Query("select r from Reservation r where (:reservationItemId is null or r.reservationItemId = :reservationItemId) "
            + "and (:status is null or r.status = :status) "
            + "and (:keyword = '' or r.reservationNumber like concat('%', :keyword, '%') "
            + "     or lower(r.reservationItemName) like lower(concat('%', :keyword, '%'))) order by r.id desc")
    Page<Reservation> search(Long reservationItemId, ReservationStatus status, String keyword, Pageable pageable);

    Page<Reservation> findByReservationNumber(String reservationNumber, Pageable pageable);

    boolean existsByReservationNumber(String reservationNumber);

    boolean existsByCheckInCode(String checkInCode);

    Optional<Reservation> findByCheckInCode(String checkInCode);

    List<Reservation> findAllByReservationItemIdAndStatusNot(Long reservationItemId, ReservationStatus status);

    List<Reservation> findAllBySessionIdAndStatusNot(Long sessionId, ReservationStatus status);

    boolean existsBySessionIdAndStatusNot(Long sessionId, ReservationStatus status);
    boolean existsByUserTypeAndUserIdAndReservationItemIdAndStatus(UserType userType, Long userId, Long reservationItemId, ReservationStatus status);

    long countByStatus(ReservationStatus status);

    @Query("select r from Reservation r where r.reservationItemId = :reservationItemId and r.queueNumber is not null "
            + "and r.checkedInAt >= :since and r.checkedInAt < :until order by r.queueNumber asc")
    List<Reservation> findQueue(Long reservationItemId, LocalDateTime since, LocalDateTime until);

    @Query("select coalesce(max(r.queueNumber), 0) from Reservation r where r.reservationItemId = :reservationItemId "
            + "and r.checkedInAt >= :since and r.checkedInAt < :until")
    int maxQueueNumber(Long reservationItemId, LocalDateTime since, LocalDateTime until);

    @Modifying
    @Query("update Reservation x set x.customStatus = null where x.customStatus = :code")
    void clearCustomStatus(String code);
}
