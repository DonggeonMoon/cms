package com.dgmoonlabs.cms.domain.archive.repository;

import com.dgmoonlabs.cms.domain.archive.dto.ArchiveRequest;
import com.dgmoonlabs.cms.domain.archive.entity.article.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArchiveCustomRepository {
    Page<Archive> find(ArchiveRequest archiveRequest, Pageable pageable);

    List<Archive> find(ArchiveRequest archiveRequest);
}
