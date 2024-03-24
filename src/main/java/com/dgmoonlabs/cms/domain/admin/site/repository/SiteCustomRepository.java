package com.dgmoonlabs.cms.domain.admin.site.repository;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteRequest;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SiteCustomRepository {
    List<Site> find(SiteRequest request, Pageable pageable);

    List<Site> find(SiteRequest siteRequest);
}
