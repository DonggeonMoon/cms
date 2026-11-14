package com.dgmoonlabs.cms.domain.course.repository;

import com.dgmoonlabs.cms.domain.course.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findAllBySiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(
            Long siteId, String keyword, Pageable pageable);

    Optional<Quiz> findByIdAndSiteIdAndDeletedAtIsNull(Long id, Long siteId);
}
