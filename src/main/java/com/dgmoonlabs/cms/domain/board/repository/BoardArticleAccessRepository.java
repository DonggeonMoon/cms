package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticleAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BoardArticleAccessRepository extends JpaRepository<BoardArticleAccess, Long> {
    boolean existsByMemberIdAndArticleIdAndRevokedAtIsNull(Long memberId, Long articleId);

    boolean existsByOrderIdAndArticleId(Long orderId, Long articleId);

    List<BoardArticleAccess> findAllByOrderIdAndArticleIdIn(Long orderId, Collection<Long> articleIds);
}
