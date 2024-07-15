package com.dgmoonlabs.cms.domain.common.code.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CodeRequest {
    private Long id;
    private Long parentId;
    private int depth;
    private int order;
    private String name;
    private String englishName;
    private String description;
    private boolean isUsing;
}
