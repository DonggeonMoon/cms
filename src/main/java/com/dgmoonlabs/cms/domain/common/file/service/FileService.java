package com.dgmoonlabs.cms.domain.common.file.service;

import com.dgmoonlabs.cms.domain.common.file.dto.FileRequest;
import com.dgmoonlabs.cms.domain.common.file.entity.File;
import com.dgmoonlabs.cms.domain.common.file.repository.FileRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    @Transactional(readOnly = true)
    public Page<File> getFile(FileRequest file, Pageable pageable) {
        return fileRepository.find(file, pageable);
    }

    @Transactional(readOnly = true)
    public Page<File> getFileWithoutPaging(File file, Pageable pageable) {
        return fileRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public File getFile(long id) {
        return fileRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateFile(HttpServletRequest request) {
    }
}
