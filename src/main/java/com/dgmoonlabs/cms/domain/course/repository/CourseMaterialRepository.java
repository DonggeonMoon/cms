package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    List<CourseMaterial> findAllByCourseIdAndLessonIdIsNullOrderBySequenceAsc(Long courseId);

    List<CourseMaterial> findAllByLessonIdOrderBySequenceAsc(Long lessonId);

    List<CourseMaterial> findAllByLessonIdInOrderBySequenceAsc(Collection<Long> lessonIds);

    Optional<CourseMaterial> findByIdAndCourseId(Long id, Long courseId);

    void deleteAllByCourseIdAndLessonIdIsNull(Long courseId);

    void deleteAllByLessonId(Long lessonId);
}
