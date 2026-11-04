package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByCourseIdAndMemberId(Long courseId, Long memberId);

    Page<CourseEnrollment> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    Page<CourseEnrollment> findAllByCourseIdOrderByIdDesc(Long courseId, Pageable pageable);
}
