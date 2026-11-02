package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardArticleRepository extends JpaRepository<BoardArticle, Long>, BoardArticleCustomRepository {
    Optional<BoardArticle> findByIdAndBoardIdAndDeletedAtIsNull(Long id, Long boardId);

    long countByDeletedAtIsNull();
}
