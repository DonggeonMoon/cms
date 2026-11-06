package com.dgmoonlabs.cms.domain.streaming.repository;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.streaming.entity.Streaming;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface StreamingRepository extends JpaRepository<Streaming, Long> {
    Optional<Streaming> findByIdAndDeletedAtIsNull(Long id);

    Optional<Streaming> findByIdAndIsUsingTrueAndDeletedAtIsNull(Long id);

    @Query("""
            select s from Streaming s
            where s.deletedAt is null
              and (:publicOnly = false or s.isUsing = true)
              and s.mediaType in :mediaTypes
              and lower(s.title) like lower(concat('%', :keyword, '%'))
            order by s.id desc
            """)
    Page<Streaming> search(String keyword, Collection<MediaType> mediaTypes, boolean publicOnly, Pageable pageable);
}
