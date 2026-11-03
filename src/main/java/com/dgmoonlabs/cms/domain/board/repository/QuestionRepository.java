package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.qna.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByIdAndDeletedAtIsNull(Long id);

    @Query("select q from Question q where q.deletedAt is null "
            + "and (:keyword = '' or lower(q.title) like lower(concat('%', :keyword, '%'))) "
            + "and (:unansweredOnly = false or not exists "
            + "(select 1 from Answer a where a.questionId = q.id and a.deletedAt is null)) order by q.id desc")
    Page<Question> search(String keyword, boolean unansweredOnly, Pageable pageable);

    @Query("select count(q) from Question q where q.deletedAt is null and not exists "
            + "(select 1 from Answer a where a.questionId = q.id and a.deletedAt is null)")
    long countUnanswered();
}
