package com.dgmoonlabs.cms.domain.common.file.repository.impl;

import com.dgmoonlabs.cms.domain.common.file.dto.FileRequest;
import com.dgmoonlabs.cms.domain.common.file.entity.File;
import com.dgmoonlabs.cms.domain.common.file.repository.FileCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FileCustomRepositoryImpl implements FileCustomRepository {
    @Override
    public Page<File> find(final FileRequest fileRequest, final Pageable pageable) {
        return null;
    }

    @Override
    public List<File> find(final FileRequest fileRequest) {
        return List.of();
    }
}