package com.dgmoonlabs.cms.domain.admin.menu.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MenuSaveRequest(
        Long parentId,
        @NotBlank @Size(max = 100) String name,
        @NotNull MenuType type,
        @Positive Integer sequence,
        @Size(max = 500) String link,
        Long targetId,
        SiteFeature feature,
        Boolean isUsing
) {
    public MenuSaveRequest {
        sequence = sequence == null ? 1 : sequence;
    }

    public boolean isUsingOrDefault() {
        return isUsing == null || isUsing;
    }

    public SiteFeature requiredFeature() {
        return type == MenuType.FEATURE ? feature : type.feature().orElse(null);
    }

    @AssertTrue(message = "링크 메뉴는 링크를, 기능 메뉴는 기능을, 컨텐츠/게시판/아카이브 메뉴는 연결 대상을 지정해야 합니다.")
    public boolean isTargetValid() {
        if (type == null) {
            return true;
        }
        return switch (type) {
            case LINK -> link != null && !link.isBlank();
            case FEATURE -> feature != null && MenuType.FEATURE_MENU_TARGETS.contains(feature);
            default -> targetId != null;
        };
    }
}
