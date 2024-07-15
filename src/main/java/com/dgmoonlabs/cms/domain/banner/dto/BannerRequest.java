package com.dgmoonlabs.cms.domain.banner.dto;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BannerRequest {
    private Long id;
    private BannerType type;
    private int order;
    private String name;
    private String description;
    private String link;
    private Boolean opensNewWindow;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean isUsing;
    private Integer width;
    private Integer height;
    private Integer top;
    private Integer left;
    private Long fileId;
}
