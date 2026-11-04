package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {
    List<CourseLesson> findAllByCourseIdAndDeletedAtIsNullOrderBySequenceAsc(Long courseId);

    List<CourseLesson> findAllByCourseIdInAndDeletedAtIsNullOrderBySequenceAsc(Collection<Long> courseIds);
}
