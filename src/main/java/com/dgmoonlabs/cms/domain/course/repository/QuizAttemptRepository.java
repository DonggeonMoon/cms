package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @Query("select distinct a.lessonId from QuizAttempt a where a.memberId = :memberId and a.passed = true and a.lessonId in :lessonIds")
    List<Long> findPassedLessonIds(Long memberId, Collection<Long> lessonIds);

    long countByLessonIdAndMemberId(Long lessonId, Long memberId);
}
