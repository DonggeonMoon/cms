package com.dgmoonlabs.cms.domain.business.attendance.repository;

import com.dgmoonlabs.cms.domain.business.attendance.entity.ClassAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClassAttendanceRepository extends JpaRepository<ClassAttendance, Long> {
    Optional<ClassAttendance> findBySubscriptionIdAndAttendedOn(Long subscriptionId, LocalDate attendedOn);

    List<ClassAttendance> findAllByProductIdAndAttendedOn(Long productId, LocalDate attendedOn);

    Page<ClassAttendance> findAllBySubscriptionIdOrderByAttendedOnDesc(Long subscriptionId, Pageable pageable);
}
