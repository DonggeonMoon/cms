package com.dgmoonlabs.cms.domain.admin.site.service;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteSaveRequest;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import com.dgmoonlabs.cms.domain.admin.site.repository.SiteRepository;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;
    private final ContentRepository contentRepository;
    private final FileService fileService;

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
        Site site = siteRepository.save(request.toEntity());
        changeSeo(site, request);
        return site.getId();
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
        changeSeo(site, request);
    }

    private void changeSeo(final Site site, final SiteSaveRequest request) {
        site.changeDarkMode(request.darkModeEnabledOrDefault());
        List<Long> images = Stream.of(request.faviconFileId(), request.ogImageFileId()).filter(Objects::nonNull).toList();
        if (!images.isEmpty()) {
            fileService.requireFilesExist(images);
        }
        site.changeSeo(request.faviconFileId(), blankToNull(request.seoTitle()), blankToNull(request.metaDescription()),
                blankToNull(request.metaKeywords()), request.ogImageFileId(), request.searchIndexingOrDefault(),
                blankToNull(request.googleVerification()), blankToNull(request.naverVerification()));
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
