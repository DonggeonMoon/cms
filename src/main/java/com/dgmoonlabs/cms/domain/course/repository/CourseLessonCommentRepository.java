package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.CourseLessonComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseLessonCommentRepository extends JpaRepository<CourseLessonComment, Long> {
    List<CourseLessonComment> findAllByLessonIdOrderByIdAsc(Long lessonId);

    Optional<CourseLessonComment> findByIdAndLessonIdAndDeletedAtIsNull(Long id, Long lessonId);

    @Query("select c from CourseLessonComment c where c.deletedAt is null and c.parentId is null "
            + "and not exists (select 1 from CourseLessonComment r where r.parentId = c.id and r.deletedAt is null) "
            + "order by c.id desc")
    Page<CourseLessonComment> findUnanswered(Pageable pageable);

    @Query("select count(c) from CourseLessonComment c where c.deletedAt is null and c.parentId is null "
            + "and not exists (select 1 from CourseLessonComment r where r.parentId = c.id and r.deletedAt is null)")
    long countUnanswered();
}
