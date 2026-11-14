package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.QuizAttemptGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptGrantRepository extends JpaRepository<QuizAttemptGrant, Long> {
    Optional<QuizAttemptGrant> findByLessonIdAndMemberId(Long lessonId, Long memberId);

    List<QuizAttemptGrant> findAllByLessonIdInAndMemberId(Collection<Long> lessonIds, Long memberId);
}
