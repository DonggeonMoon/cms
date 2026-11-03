package com.dgmoonlabs.cms.domain.archive.repository;

import com.dgmoonlabs.cms.domain.archive.entity.comment.ArchiveComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArchiveCommentRepository extends JpaRepository<ArchiveComment, Long> {
    List<ArchiveComment> findAllByArticleIdAndDeletedAtIsNullOrderByIdAsc(Long articleId);

    List<ArchiveComment> findAllByArticleIdOrderByIdAsc(Long articleId);

    Optional<ArchiveComment> findByIdAndArticleIdAndDeletedAtIsNull(Long id, Long articleId);
}
