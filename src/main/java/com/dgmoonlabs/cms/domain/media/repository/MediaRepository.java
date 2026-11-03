package com.dgmoonlabs.cms.domain.media.repository;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.media.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByIdAndDeletedAtIsNull(Long id);

    Optional<Media> findByIdAndIsUsingTrueAndDeletedAtIsNull(Long id);

    @Query("""
            select m from Media m
            where m.deletedAt is null
              and (:publicOnly = false or m.isUsing = true)
              and m.mediaType in :mediaTypes
              and lower(m.title) like lower(concat('%', :keyword, '%'))
            order by m.id desc
            """)
    Page<Media> search(String keyword, Collection<MediaType> mediaTypes, boolean publicOnly, Pageable pageable);
}
