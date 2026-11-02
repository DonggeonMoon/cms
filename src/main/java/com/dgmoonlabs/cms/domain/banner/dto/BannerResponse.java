package com.dgmoonlabs.cms.domain.banner.dto;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;

import java.time.LocalDateTime;

public record BannerResponse(
        Long id,
        BannerType type,
        int order,
        String name,
        String description,
        String link,
        boolean opensNewWindow,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        boolean isUsing,
        Integer width,
        Integer height,
        Integer top,
        Integer left,
        Long fileId,
        String imageUrl,
        String backgroundColor,
        String textColor,
        boolean isModal,
        int dismissDays
) {
    public static BannerResponse from(final Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getType(),
                banner.getOrder(),
                banner.getName(),
                banner.getDescription(),
                banner.getLink(),
                banner.isOpensNewWindow(),
                banner.getStartDateTime(),
                banner.getEndDateTime(),
                banner.isUsing(),
                banner.getWidth(),
                banner.getHeight(),
                banner.getTop(),
                banner.getLeft(),
                banner.getFileId(),
                banner.getFileId() == null ? null : "/api/files/" + banner.getFileId() + "/view",
                banner.getBackgroundColor(),
                banner.getTextColor(),
                banner.isModal(),
                banner.getDismissDays()
        );
    }
}
