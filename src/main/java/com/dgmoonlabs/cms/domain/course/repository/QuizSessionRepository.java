package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByLessonIdAndMemberId(Long lessonId, Long memberId);

    void deleteByLessonIdAndMemberId(Long lessonId, Long memberId);
}
