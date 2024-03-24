package com.dgmoonlabs.cms.domain.admin.site.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class SiteRequest {
    private String name;

    private String description;

    private String domain;

    private MenuType type;

    private boolean isDefault;

    private String locale;
}
