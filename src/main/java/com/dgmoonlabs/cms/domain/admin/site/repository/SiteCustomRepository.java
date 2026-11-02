package com.dgmoonlabs.cms.domain.admin.site.repository;

import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SiteCustomRepository {
    Page<Site> search(String keyword, Pageable pageable);
}
