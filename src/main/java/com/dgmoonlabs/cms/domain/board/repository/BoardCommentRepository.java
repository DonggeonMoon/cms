package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.comment.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findAllByArticleIdOrderByIdAsc(Long articleId);

    Optional<BoardComment> findByIdAndArticleIdAndDeletedAtIsNull(Long id, Long articleId);

    @Query("select c.articleId, count(c) from BoardComment c "
            + "where c.articleId in :articleIds and c.deletedAt is null group by c.articleId")
    List<Object[]> countByArticleIds(Collection<Long> articleIds);
}
