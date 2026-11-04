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
    Optional<Content> findByIdAndDeletedAtIsNull(Long id);

    List<Content> findAllByStatusAndPublishAtLessThanEqualAndDeletedAtIsNull(ContentStatus status, LocalDateTime now);

    Page<Content> findAllByStatusAndDeletedAtIsNullOrderByReviewRequestedAtAsc(ContentStatus status, Pageable pageable);

    Optional<Content> findByIdAndStatusAndDeletedAtIsNull(Long id, ContentStatus status);

    List<Content> findAllByStatusAndDeletedAtIsNullOrderByIdAsc(ContentStatus status);

    Page<Content> findAllByDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdAsc(String keyword, Pageable pageable);
}
