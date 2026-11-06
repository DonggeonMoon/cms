package com.dgmoonlabs.cms.domain.streaming.repository;

import com.dgmoonlabs.cms.domain.streaming.entity.StreamingProgress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StreamingProgressRepository extends JpaRepository<StreamingProgress, Long> {
    Optional<StreamingProgress> findByStreamingIdAndMemberId(Long streamingId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from StreamingProgress p where p.streamingId = :streamingId and p.memberId = :memberId")
    Optional<StreamingProgress> findForUpdate(Long streamingId, Long memberId);

    List<StreamingProgress> findAllByMemberIdAndStreamingIdIn(Long memberId, Collection<Long> streamingIds);

    List<StreamingProgress> findAllByStreamingIdAndMemberIdIn(Long streamingId, Collection<Long> memberIds);
}
