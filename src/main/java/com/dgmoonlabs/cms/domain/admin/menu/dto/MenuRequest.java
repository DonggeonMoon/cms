package com.dgmoonlabs.cms.domain.admin.menu.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class MenuRequest {
    private Long id;
    private String name;
    private MenuType type;
    private Long parentId;
    private int depth;
    private int sequence;
    private String link;
}
