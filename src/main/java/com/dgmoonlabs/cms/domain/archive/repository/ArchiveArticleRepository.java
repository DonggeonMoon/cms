package com.dgmoonlabs.cms.domain.archive.repository;

import com.dgmoonlabs.cms.domain.archive.constant.ArchiveStatus;
import com.dgmoonlabs.cms.domain.archive.entity.article.ArchiveArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArchiveArticleRepository extends JpaRepository<ArchiveArticle, Long> {
    Optional<ArchiveArticle> findByIdAndArchiveIdAndDeletedAtIsNull(Long id, Long archiveId);

    @Query("select a from ArchiveArticle a where a.archiveId = :archiveId and a.deletedAt is null "
            + "and (:status is null or a.status = :status) "
            + "and (:keyword = '' or lower(a.title) like lower(concat('%', :keyword, '%')) "
            + "     or lower(a.contentText) like lower(concat('%', :keyword, '%'))) "
            + "order by a.id desc")
    Page<ArchiveArticle> search(Long archiveId, ArchiveStatus status, String keyword, Pageable pageable);
}
