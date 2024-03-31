package com.dgmoonlabs.cms.domain.admin.site.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class SiteRequest {
    private Long id;
    private String name;
    private String description;
    private String domain;
    private String theme;
    private MenuType type;
    private String locale;
    private Boolean isDefault;
}
