package com.dgmoonlabs.cms.domain.archive.repository;

import com.dgmoonlabs.cms.domain.archive.entity.article.ArchiveArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveArticleLikeRepository extends JpaRepository<ArchiveArticleLike, Long> {
    boolean existsByArticleIdAndMemberId(Long articleId, Long memberId);
}
