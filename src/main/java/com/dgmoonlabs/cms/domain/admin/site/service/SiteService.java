package com.dgmoonlabs.cms.domain.admin.site.service;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteSaveRequest;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import com.dgmoonlabs.cms.domain.admin.site.repository.SiteRepository;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.site.SiteContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SiteService {
    /** 사이트별로 나뉘는 테이블 - 사이트 설정 전에 만들어져 site_id 가 비어 있는 행은 새로 지정된 기본 사이트가 가져간다. */
    static final List<String> SITE_SCOPED_TABLES = List.of("menu", "banner", "content", "board", "course", "reservation_item");

    private final SiteRepository siteRepository;
    private final ContentRepository contentRepository;
    private final FileService fileService;
    private final JdbcTemplate jdbcTemplate;

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
    public SiteResponse getCurrentSite() {
        Long siteId = SiteContextHolder.currentSiteId();
        if (siteId == null) {
            return getDefaultSite();
        }
        return siteRepository.findByIdAndDeletedAtIsNull(siteId).map(SiteResponse::from).orElseGet(SiteResponse::fallback);
    }

    @Transactional(readOnly = true)
    public boolean isEnabled(final SiteFeature feature) {
        return currentOrDefaultSite().map(site -> site.isEnabled(feature)).orElse(true);
    }

    /** /sitemap.xml, /robots.txt 처럼 X-CMS-Site 헤더를 보는 인터셉터가 지나가지 않는 경로를 위한 안전한 현재 사이트 ID. */
    @Transactional(readOnly = true)
    public Long currentSiteId() {
        Long siteId = SiteContextHolder.currentSiteId();
        return siteId != null ? siteId : defaultSiteId();
    }

    @Transactional(readOnly = true)
    public Long resolveSiteId(final String code) {
        if (code == null || code.isBlank()) {
            return defaultSiteId();
        }
        return siteRepository.findByCodeAndDeletedAtIsNull(code).map(Site::getId).orElseThrow(() -> new BusinessException(ErrorCode.SITE_NOT_FOUND));
    }

    private Long defaultSiteId() {
        return siteRepository.findFirstByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc().map(Site::getId).orElse(null);
    }

    private Optional<Site> currentOrDefaultSite() {
        Long siteId = SiteContextHolder.currentSiteId();
        if (siteId != null) {
            return siteRepository.findByIdAndDeletedAtIsNull(siteId);
        }
        return siteRepository.findFirstByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc();
    }

    @Transactional
    public Long createSite(final SiteSaveRequest request) {
        validateHomeContent(request, SiteContextHolder.currentSiteId());
        validateCode(request.codeOrNull(), null);
        if (request.isDefault()) {
            clearDefault();
        }
        Site site = siteRepository.save(request.toEntity());
        changeSeo(site, request);
        if (site.isDefault()) {
            claimOrphanedRows(site.getId());
        }
        return site.getId();
    }

    /** 사이트 설정 전에 만들어져 어느 사이트에도 속하지 않던 메뉴·배너·컨텐츠 등을 새 기본 사이트로 편입한다. */
    void claimOrphanedRows(final Long siteId) {
        for (String table : SITE_SCOPED_TABLES) {
            jdbcTemplate.update("update " + table + " set site_id = ? where site_id is null", siteId);
        }
    }

    @Transactional
    public void updateSite(final Long id, final SiteSaveRequest request) {
        validateHomeContent(request, id);
        validateCode(request.codeOrNull(), id);
        Site site = findSite(id);
        if (request.isDefault() && !site.isDefault()) {
            clearDefault();
        }
        site.update(request.name(), request.description(), request.domain(), request.codeOrNull(), request.theme(),
                request.type(), request.locale(), request.isDefault(), request.homeContentId(), request.disabledFeaturesOrEmpty());
        changeSeo(site, request);
        if (site.isDefault()) {
            claimOrphanedRows(site.getId());
        }
    }

    private void validateCode(final String code, final Long id) {
        if (code == null) {
            return;
        }
        boolean duplicate = id == null ? siteRepository.existsByCodeAndDeletedAtIsNull(code)
                : siteRepository.existsByCodeAndDeletedAtIsNullAndIdNot(code, id);
        if (duplicate) {
            throw new BusinessException(ErrorCode.DUPLICATE_SITE_CODE);
        }
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

    private void validateHomeContent(final SiteSaveRequest request, final Long siteId) {
        if (request.homeContentId() != null
                && contentRepository.findByIdAndSiteIdAndDeletedAtIsNull(request.homeContentId(), siteId).isEmpty()) {
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
