package com.dgmoonlabs.cms.domain.archive.service;

import com.dgmoonlabs.cms.domain.archive.dto.ArchiveRequest;
import com.dgmoonlabs.cms.domain.archive.entity.article.Archive;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchiveService {
    private final ArchiveRepository archiveRepository;

    @Transactional(readOnly = true)
    public Page<Archive> getArchive(ArchiveRequest archiveRequest, Pageable pageable) {
        return archiveRepository.find(archiveRequest, pageable);
    }

    @Transactional(readOnly = true)
    public List<Archive> getArchiveWithoutPaging(Archive archive, Pageable pageable) {
        return archiveRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Archive getArchive(long id) {
        return archiveRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateArchive(HttpServletRequest request) {
    }
}
