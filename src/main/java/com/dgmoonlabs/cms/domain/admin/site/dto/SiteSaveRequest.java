package com.dgmoonlabs.cms.domain.admin.site.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

@Builder
public record SiteSaveRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @Size(max = 255) String domain,
        @Size(max = 50) String theme,
        MenuType type,
        @Size(max = 20) String locale,
        boolean isDefault,
        Long homeContentId,
        Set<SiteFeature> disabledFeatures
) {
    public Set<SiteFeature> disabledFeaturesOrEmpty() {
        return disabledFeatures == null ? Set.of() : disabledFeatures;
    }

    public Site toEntity() {
        return Site.builder()
                .name(name)
                .description(description)
                .domain(domain)
                .theme(theme)
                .type(type)
                .locale(locale)
                .isDefault(isDefault)
                .homeContentId(homeContentId)
                .disabledFeatures(new HashSet<>(disabledFeaturesOrEmpty()))
                .build();
    }
}
