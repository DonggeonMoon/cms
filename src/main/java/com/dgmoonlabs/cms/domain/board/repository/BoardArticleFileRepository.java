package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticleFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BoardArticleFileRepository extends JpaRepository<BoardArticleFile, Long> {
    List<BoardArticleFile> findAllByArticleIdOrderBySequenceAsc(Long articleId);

    List<BoardArticleFile> findAllByArticleIdInOrderBySequenceAsc(Collection<Long> articleIds);

    void deleteAllByArticleId(Long articleId);
}
