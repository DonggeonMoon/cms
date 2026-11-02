package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.qna.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByQuestionIdAndDeletedAtIsNull(Long questionId);

    List<Answer> findAllByQuestionIdInAndDeletedAtIsNull(Collection<Long> questionIds);

    Optional<Answer> findByQuestionId(Long questionId);
}
