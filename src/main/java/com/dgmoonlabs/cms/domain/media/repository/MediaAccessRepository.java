package com.dgmoonlabs.cms.domain.media.repository;

import com.dgmoonlabs.cms.domain.media.entity.MediaAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface MediaAccessRepository extends JpaRepository<MediaAccess, Long> {
    @Query("select a from MediaAccess a where a.memberId = :memberId and a.mediaId in :mediaIds and a.revokedAt is null "
            + "and (a.expiresAt is null or a.expiresAt > :now)")
    List<MediaAccess> findActive(Long memberId, Collection<Long> mediaIds, LocalDateTime now);

    boolean existsByOrderIdAndMediaId(Long orderId, Long mediaId);

    List<MediaAccess> findAllByOrderIdAndMediaIdIn(Long orderId, Collection<Long> mediaIds);
}
