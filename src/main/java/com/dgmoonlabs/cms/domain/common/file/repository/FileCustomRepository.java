package com.dgmoonlabs.cms.domain.common.file.repository;

import com.dgmoonlabs.cms.domain.common.file.dto.FileRequest;
import com.dgmoonlabs.cms.domain.common.file.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FileCustomRepository {
    Page<File> find(FileRequest fileRequest, Pageable pageable);

    List<File> find(FileRequest fileRequest);
}
