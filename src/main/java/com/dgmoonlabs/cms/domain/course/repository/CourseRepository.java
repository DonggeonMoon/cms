package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByIdAndDeletedAtIsNull(Long id);

    Page<Course> findAllByDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(String keyword, Pageable pageable);

    Page<Course> findAllByIsUsingTrueAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(String keyword, Pageable pageable);
}
