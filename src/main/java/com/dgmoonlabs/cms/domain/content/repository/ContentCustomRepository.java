package com.dgmoonlabs.cms.domain.content.repository;

import com.dgmoonlabs.cms.domain.content.dto.ContentRequest;
import com.dgmoonlabs.cms.domain.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentCustomRepository {
    Page<Content> find(ContentRequest contentRequest, Pageable pageable);

    List<Content> find(ContentRequest contentRequest);
}
