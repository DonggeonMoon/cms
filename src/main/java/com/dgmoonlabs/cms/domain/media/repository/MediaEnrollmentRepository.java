package com.dgmoonlabs.cms.domain.media.repository;

import com.dgmoonlabs.cms.domain.media.entity.MediaEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MediaEnrollmentRepository extends JpaRepository<MediaEnrollment, Long> {
    Optional<MediaEnrollment> findByMediaIdAndMemberId(Long mediaId, Long memberId);

    List<MediaEnrollment> findAllByMemberIdAndMediaIdIn(Long memberId, Collection<Long> mediaIds);

    Page<MediaEnrollment> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    Page<MediaEnrollment> findAllByMediaIdOrderByIdDesc(Long mediaId, Pageable pageable);
}
