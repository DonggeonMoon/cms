package com.dgmoonlabs.cms.domain.content.repository.impl;

import com.dgmoonlabs.cms.domain.content.dto.ContentRequest;
import com.dgmoonlabs.cms.domain.content.entity.Content;
import com.dgmoonlabs.cms.domain.content.repository.ContentCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class ContentCustomRepositoryImpl implements ContentCustomRepository {
    @Override
    public Page<Content> find(final ContentRequest contentRequest, final Pageable pageable) {
        return null;
    }

    @Override
    public List<Content> find(final ContentRequest contentRequest) {
        return List.of();
    }
}
