package com.dgmoonlabs.cms.domain.content.service;

import com.dgmoonlabs.cms.domain.content.dto.ContentRequest;
import com.dgmoonlabs.cms.domain.content.entity.Content;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public Page<Content> getContent(ContentRequest content, Pageable pageable) {
        return contentRepository.find(content, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Content> getContentWithoutPaging(Content Content, Pageable pageable) {
        return contentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Content getContent(long id) {
        return contentRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateContent(HttpServletRequest request, ContentRequest contentRequest) {
        String userAgent = request.getHeader("USER-AGENT");
    }
}
