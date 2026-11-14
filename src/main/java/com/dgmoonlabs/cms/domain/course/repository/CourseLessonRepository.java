package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {
    List<CourseLesson> findAllByCourseIdAndDeletedAtIsNullOrderBySequenceAsc(Long courseId);

    Optional<CourseLesson> findByIdAndCourseIdAndDeletedAtIsNull(Long id, Long courseId);

    List<CourseLesson> findAllByCourseIdInAndDeletedAtIsNullOrderBySequenceAsc(Collection<Long> courseIds);

    boolean existsByQuizIdAndDeletedAtIsNull(Long quizId);

    List<CourseLesson> findAllByQuizIdInAndDeletedAtIsNull(Collection<Long> quizIds);
}
