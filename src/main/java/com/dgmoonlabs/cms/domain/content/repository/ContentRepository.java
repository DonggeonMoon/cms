package com.dgmoonlabs.cms.domain.content.repository;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.domain.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByIdAndSiteIdAndDeletedAtIsNull(Long id, Long siteId);

    /** 사이트 구분 없이 전체 사이트의 예약 발행을 처리하는 배치용 — siteId 필터를 두지 않는다. */
    List<Content> findAllByStatusAndPublishAtLessThanEqualAndDeletedAtIsNull(ContentStatus status, LocalDateTime now);

    Page<Content> findAllByStatusAndSiteIdAndDeletedAtIsNullOrderByReviewRequestedAtAsc(ContentStatus status, Long siteId, Pageable pageable);

    Optional<Content> findByIdAndStatusAndSiteIdAndDeletedAtIsNull(Long id, ContentStatus status, Long siteId);

    List<Content> findAllByStatusAndSiteIdAndDeletedAtIsNullOrderByIdAsc(ContentStatus status, Long siteId);

    Page<Content> findAllBySiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdAsc(Long siteId, String keyword, Pageable pageable);
}
