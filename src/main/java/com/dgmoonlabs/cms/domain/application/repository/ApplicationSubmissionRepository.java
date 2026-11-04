package com.dgmoonlabs.cms.domain.application.repository;

import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ApplicationSubmissionRepository extends JpaRepository<ApplicationSubmission, Long> {
    @Query("select count(s) from ApplicationSubmission s where s.formId = :formId and s.status <> :excluded")
    long countActive(Long formId, SubmissionStatus excluded);

    boolean existsByFormIdAndMemberIdAndStatusNot(Long formId, Long memberId, SubmissionStatus status);

    Optional<ApplicationSubmission> findByIdAndMemberId(Long id, Long memberId);

    Page<ApplicationSubmission> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    @Query("select s from ApplicationSubmission s where s.formId = :formId and (:status is null or s.status = :status) order by s.id desc")
    Page<ApplicationSubmission> search(Long formId, SubmissionStatus status, Pageable pageable);
}
