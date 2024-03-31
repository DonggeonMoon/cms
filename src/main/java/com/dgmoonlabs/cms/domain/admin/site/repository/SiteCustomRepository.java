package com.dgmoonlabs.cms.domain.admin.site.repository;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteRequest;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SiteCustomRepository {
    Page<Site> find(SiteRequest request, Pageable pageable);

    List<Site> find(SiteRequest siteRequest);
}
