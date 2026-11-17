package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByIdAndSiteIdAndDeletedAtIsNull(Long id, Long siteId);

    List<Course> findAllByIsUsingTrueAndSiteIdAndDeletedAtIsNullOrderByIdAsc(Long siteId);

    List<Course> findAllBySiteIdAndDeletedAtIsNullOrderByIdDesc(Long siteId);

    @Query("select c from Course c where (c.siteId = :siteId or (c.siteId is null and :siteId is null)) and c.deletedAt is null "
            + "and lower(c.title) like lower(concat('%', :keyword, '%')) and (:isCourse is null or c.isCourse = :isCourse) "
            + "order by c.id desc")
    Page<Course> searchAdmin(Long siteId, String keyword, Boolean isCourse, Pageable pageable);

    @Query("select c from Course c where c.isUsing = true and (c.siteId = :siteId or (c.siteId is null and :siteId is null)) "
            + "and c.deletedAt is null and lower(c.title) like lower(concat('%', :keyword, '%')) "
            + "and (:isCourse is null or c.isCourse = :isCourse) order by c.id desc")
    Page<Course> searchPublic(Long siteId, String keyword, Boolean isCourse, Pageable pageable);

    long countByCategoryIdAndDeletedAtIsNull(Long categoryId);
}
