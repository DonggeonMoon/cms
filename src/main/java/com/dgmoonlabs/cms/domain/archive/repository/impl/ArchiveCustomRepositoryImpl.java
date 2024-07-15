package com.dgmoonlabs.cms.domain.archive.repository.impl;

import com.dgmoonlabs.cms.domain.archive.dto.ArchiveRequest;
import com.dgmoonlabs.cms.domain.archive.entity.article.Archive;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class ArchiveCustomRepositoryImpl implements ArchiveCustomRepository {
    @Override
    public Page<Archive> find(final ArchiveRequest archiveRequest, final Pageable pageable) {
        return null;
    }

    @Override
    public List<Archive> find(final ArchiveRequest archiveRequest) {
        return List.of();
    }
}
