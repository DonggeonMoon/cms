package com.dgmoonlabs.cms.domain.admin.menu.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class MenuTemplateRequest {
    private String menuHeader;

    private String menuTemplate;

    private String menuFooter;
}
