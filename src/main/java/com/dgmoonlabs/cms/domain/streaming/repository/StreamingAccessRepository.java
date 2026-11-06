package com.dgmoonlabs.cms.domain.streaming.repository;

import com.dgmoonlabs.cms.domain.streaming.entity.StreamingAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StreamingAccessRepository extends JpaRepository<StreamingAccess, Long> {
    @Query("select a from StreamingAccess a where a.memberId = :memberId and a.streamingId in :streamingIds and a.revokedAt is null "
            + "and (a.expiresAt is null or a.expiresAt > :now)")
    List<StreamingAccess> findActive(Long memberId, Collection<Long> streamingIds, LocalDateTime now);

    boolean existsByOrderIdAndStreamingId(Long orderId, Long streamingId);

    List<StreamingAccess> findAllByOrderIdAndStreamingIdIn(Long orderId, Collection<Long> streamingIds);
}
