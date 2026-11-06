package com.dgmoonlabs.cms.domain.streaming.repository;

import com.dgmoonlabs.cms.domain.streaming.entity.StreamingEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StreamingEnrollmentRepository extends JpaRepository<StreamingEnrollment, Long> {
    Optional<StreamingEnrollment> findByStreamingIdAndMemberId(Long streamingId, Long memberId);

    List<StreamingEnrollment> findAllByMemberIdAndStreamingIdIn(Long memberId, Collection<Long> streamingIds);

    Page<StreamingEnrollment> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    Page<StreamingEnrollment> findAllByStreamingIdOrderByIdDesc(Long streamingId, Pageable pageable);
}
