package com.dgmoonlabs.cms.domain.archive.repository;

import com.dgmoonlabs.cms.domain.archive.entity.article.ArchiveArticleFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ArchiveArticleFileRepository extends JpaRepository<ArchiveArticleFile, Long> {
    List<ArchiveArticleFile> findAllByArticleIdOrderBySequenceAsc(Long articleId);

    List<ArchiveArticleFile> findAllByArticleIdInOrderBySequenceAsc(Collection<Long> articleIds);

    void deleteAllByArticleId(Long articleId);
}
