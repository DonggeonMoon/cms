package com.dgmoonlabs.cms.domain.admin.site.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public record SiteResponse(
        Long id,
        String name,
        String description,
        String domain,
        String theme,
        MenuType type,
        String locale,
        boolean isDefault,
        Long homeContentId,
        Map<SiteFeature, Boolean> features,
        Long faviconFileId,
        String faviconUrl,
        String seoTitle,
        String metaDescription,
        String metaKeywords,
        Long ogImageFileId,
        String ogImageUrl,
        boolean searchIndexing,
        String googleVerification,
        String naverVerification,
        LocalDateTime createdAt
) {
    public static final String FALLBACK_NAME = "cms";

    public static SiteResponse from(final Site site) {
        return new SiteResponse(
                site.getId(),
                site.getName(),
                site.getDescription(),
                site.getDomain(),
                site.getTheme(),
                site.getType(),
                site.getLocale(),
                site.isDefault(),
                site.getHomeContentId(),
                features(site.getDisabledFeatures()),
                site.getFaviconFileId(),
                fileUrl(site.getFaviconFileId()),
                site.getSeoTitle(),
                site.getMetaDescription(),
                site.getMetaKeywords(),
                site.getOgImageFileId(),
                fileUrl(site.getOgImageFileId()),
                site.isSearchIndexing(),
                site.getGoogleVerification(),
                site.getNaverVerification(),
                site.getCreatedAt()
        );
    }

    public static SiteResponse fallback() {
        return new SiteResponse(null, FALLBACK_NAME, null, null, null, null, null, true, null, features(Set.of()),
                null, null, null, null, null, null, null, true, null, null, null);
    }

    public String titleOrName() {
        return seoTitle == null || seoTitle.isBlank() ? name : seoTitle;
    }

    private static String fileUrl(final Long fileId) {
        return fileId == null ? null : "/api/files/" + fileId + "/view";
    }

    private static Map<SiteFeature, Boolean> features(final Set<SiteFeature> disabled) {
        Map<SiteFeature, Boolean> features = new EnumMap<>(SiteFeature.class);
        Arrays.stream(SiteFeature.values()).forEach(feature -> features.put(feature, !disabled.contains(feature)));
        return features;
    }
}
