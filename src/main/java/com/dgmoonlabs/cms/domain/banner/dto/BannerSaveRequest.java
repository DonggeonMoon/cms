package com.dgmoonlabs.cms.domain.banner.dto;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BannerSaveRequest(
        @NotNull BannerType type,
        @Positive Integer order,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @Size(max = 500) String link,
        boolean opensNewWindow,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        boolean isUsing,
        @PositiveOrZero Integer width,
        @PositiveOrZero Integer height,
        Integer top,
        Integer left,
        Long fileId,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다.") String backgroundColor,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다.") String textColor,
        boolean isModal,
        @Min(0) @Max(365) Integer dismissDays
) {
    public BannerSaveRequest {
        order = order == null ? 1 : order;
    }

    public int dismissDaysOrDefault() {
        return dismissDays == null ? 1 : dismissDays;
    }

    public boolean hasValidPeriod() {
        return startDateTime == null || endDateTime == null || endDateTime.isAfter(startDateTime);
    }

    public Banner toEntity() {
        return Banner.builder()
                .type(type)
                .order(order)
                .name(name)
                .description(description)
                .link(link)
                .opensNewWindow(opensNewWindow)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .isUsing(isUsing)
                .width(width)
                .height(height)
                .top(top)
                .left(left)
                .fileId(fileId)
                .backgroundColor(backgroundColor)
                .textColor(textColor)
                .isModal(isModal)
                .dismissDays(dismissDaysOrDefault())
                .build();
    }
}
