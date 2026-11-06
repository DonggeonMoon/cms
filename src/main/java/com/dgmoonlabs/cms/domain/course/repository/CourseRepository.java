package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByIdAndSiteIdAndDeletedAtIsNull(Long id, Long siteId);

    List<Course> findAllByIsUsingTrueAndSiteIdAndDeletedAtIsNullOrderByIdAsc(Long siteId);

    Page<Course> findAllBySiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(Long siteId, String keyword, Pageable pageable);

    Page<Course> findAllByIsUsingTrueAndSiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(Long siteId, String keyword, Pageable pageable);
}
