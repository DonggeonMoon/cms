package com.dgmoonlabs.cms.domain.admin.site.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class SiteResponse {
    private Long id;
    private String name;
    private String description;
    private String domain;
    private String theme;
    private MenuType type;
    private String locale;
    private Boolean isDefault;

    public static SiteResponse from(Site site) {
        return SiteResponse.builder()
                .id(site.getId())
                .name(site.getName())
                .domain(site.getDomain())
                .theme(site.getTheme())
                .type(site.getType())
                .locale(site.getLocale())
                .isDefault(site.isDefault())
                .build();
    }
}
