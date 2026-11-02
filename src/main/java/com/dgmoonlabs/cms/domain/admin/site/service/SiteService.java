package com.dgmoonlabs.cms.domain.admin.site.service;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteSaveRequest;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import com.dgmoonlabs.cms.domain.admin.site.repository.SiteRepository;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;
    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public Page<SiteResponse> searchSites(final String keyword, final Pageable pageable) {
        return siteRepository.search(keyword, pageable).map(SiteResponse::from);
    }

    @Transactional(readOnly = true)
    public SiteResponse getSite(final Long id) {
        return SiteResponse.from(findSite(id));
    }

    @Transactional(readOnly = true)
    public SiteResponse getDefaultSite() {
        return siteRepository.findFirstByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc()
                .map(SiteResponse::from)
                .orElseGet(SiteResponse::fallback);
    }

    @Transactional(readOnly = true)
    public boolean isEnabled(final SiteFeature feature) {
        return siteRepository.findFirstByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc()
                .map(site -> site.isEnabled(feature))
                .orElse(true);
    }

    @Transactional
    public Long createSite(final SiteSaveRequest request) {
        validateHomeContent(request);
        if (request.isDefault()) {
            clearDefault();
        }
        return siteRepository.save(request.toEntity()).getId();
    }

    @Transactional
    public void updateSite(final Long id, final SiteSaveRequest request) {
        validateHomeContent(request);
        Site site = findSite(id);
        if (request.isDefault() && !site.isDefault()) {
            clearDefault();
        }
        site.update(request.name(), request.description(), request.domain(), request.theme(),
                request.type(), request.locale(), request.isDefault(), request.homeContentId(), request.disabledFeaturesOrEmpty());
    }

    @Transactional
    public void deleteSite(final Long id) {
        findSite(id).delete();
    }

    private void validateHomeContent(final SiteSaveRequest request) {
        if (request.homeContentId() != null && contentRepository.findByIdAndDeletedAtIsNull(request.homeContentId()).isEmpty()) {
            throw BusinessException.notFound();
        }
    }

    private void clearDefault() {
        siteRepository.findAllByIsDefaultTrueAndDeletedAtIsNull().forEach(Site::unmarkDefault);
    }

    private Site findSite(final Long id) {
        return siteRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
