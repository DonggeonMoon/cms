package com.dgmoonlabs.cms.domain.common.file.repository;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FileCustomRepository {
    Page<File> search(String keyword, MediaType mediaType, Pageable pageable);
}
