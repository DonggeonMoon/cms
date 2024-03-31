package com.dgmoonlabs.cms.domain.admin.site.service;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteRequest;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import com.dgmoonlabs.cms.domain.admin.site.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;

    public Long saveSite(SiteRequest request) {
        return siteRepository.save(request.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public Page<SiteResponse> getSite(SiteRequest request, Pageable pageable) {
        return siteRepository.find(request, pageable).map(SiteResponse::from);
    }

    @Transactional(readOnly = true)
    public List<Site> getSitesWithoutPaging(SiteRequest siteRequest) {
        return siteRepository.find(siteRequest);
    }

    @Transactional(readOnly = true)
    public Site getSiteById(long id) {
        return siteRepository.findById(id)
                .orElseThrow(RuntimeException::new);
    }

    public void updateSite(SiteRequest request) {
        Site site = siteRepository.findById(request.getId()).orElseThrow(RuntimeException::new);
        site.update(
                request.getName(),
                request.getDescription(),
                request.getDomain(),
                request.getTheme(),
                request.getType(),
                request.getLocale(),
                request.getIsDefault()
        );
    }

    public void deleteSiteById(Long id) {
        siteRepository.deleteById(id);
    }

    public void deleteSitesByIds(List<Long> ids) {
        siteRepository.deleteAllById(ids);
    }
}
