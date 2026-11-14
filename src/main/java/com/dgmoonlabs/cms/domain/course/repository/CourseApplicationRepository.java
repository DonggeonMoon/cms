package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.CourseApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseApplicationRepository extends JpaRepository<CourseApplication, Long> {
    Optional<CourseApplication> findFirstByCourseIdAndMemberIdOrderByIdDesc(Long courseId, Long memberId);

    Optional<CourseApplication> findByIdAndCourseId(Long id, Long courseId);

    Page<CourseApplication> findAllByCourseIdOrderByIdDesc(Long courseId, Pageable pageable);

    List<CourseApplication> findAllByCourseIdIn(Collection<Long> courseIds);
}
