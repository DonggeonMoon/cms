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
            + "and (:keyword = '' or lower(q.title) like lower(concat('%', :keyword, '%'))) order by q.id desc")
    Page<Question> search(String keyword, Pageable pageable);

    long countByDeletedAtIsNull();
}
