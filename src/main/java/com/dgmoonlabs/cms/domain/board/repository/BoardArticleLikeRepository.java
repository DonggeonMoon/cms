package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardArticleLikeRepository extends JpaRepository<BoardArticleLike, Long> {
    boolean existsByArticleIdAndMemberId(Long articleId, Long memberId);
}
