package com.dgmoonlabs.cms.domain.admin.site.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

@Builder
public record SiteSaveRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @Size(max = 255) String domain,
        @Size(max = 50) @Pattern(regexp = "^[a-z0-9-]*$") String code,
        @Size(max = 50) String theme,
        MenuType type,
        @Size(max = 20) String locale,
        boolean isDefault,
        Long homeContentId,
        Set<SiteFeature> disabledFeatures,
        Boolean darkModeEnabled,
        Long faviconFileId,
        @Size(max = 100) String seoTitle,
        @Size(max = 300) String metaDescription,
        @Size(max = 300) String metaKeywords,
        Long ogImageFileId,
        Boolean searchIndexing,
        @Size(max = 100) @Pattern(regexp = "^[A-Za-z0-9_-]*$") String googleVerification,
        @Size(max = 100) @Pattern(regexp = "^[A-Za-z0-9_-]*$") String naverVerification
) {
    public boolean darkModeEnabledOrDefault() {
        return darkModeEnabled == null || darkModeEnabled;
    }

    public boolean searchIndexingOrDefault() {
        return searchIndexing == null || searchIndexing;
    }

    public Set<SiteFeature> disabledFeaturesOrEmpty() {
        return disabledFeatures == null ? Set.of() : disabledFeatures;
    }

    public String codeOrNull() {
        return code == null || code.isBlank() ? null : code;
    }

    public Site toEntity() {
        return Site.builder()
                .name(name)
                .description(description)
                .domain(domain)
                .code(codeOrNull())
                .theme(theme)
                .type(type)
                .locale(locale)
                .isDefault(isDefault)
                .homeContentId(homeContentId)
                .disabledFeatures(new HashSet<>(disabledFeaturesOrEmpty()))
                .build();
    }
}
