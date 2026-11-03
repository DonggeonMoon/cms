package com.dgmoonlabs.cms.domain.media.repository;

import com.dgmoonlabs.cms.domain.media.entity.MediaProgress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MediaProgressRepository extends JpaRepository<MediaProgress, Long> {
    Optional<MediaProgress> findByMediaIdAndMemberId(Long mediaId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from MediaProgress p where p.mediaId = :mediaId and p.memberId = :memberId")
    Optional<MediaProgress> findForUpdate(Long mediaId, Long memberId);

    List<MediaProgress> findAllByMemberIdAndMediaIdIn(Long memberId, Collection<Long> mediaIds);

    List<MediaProgress> findAllByMediaIdAndMemberIdIn(Long mediaId, Collection<Long> memberIds);
}
