package com.dgmoonlabs.cms.domain.admin.menu.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MenuSaveRequest(
        Long parentId,
        @NotBlank @Size(max = 100) String name,
        @NotNull MenuType type,
        @PositiveOrZero int sequence,
        @Size(max = 500) String link,
        Long targetId,
        Boolean isUsing
) {
    public boolean isUsingOrDefault() {
        return isUsing == null || isUsing;
    }

    @AssertTrue(message = "링크 메뉴는 링크를, 컨텐츠/게시판 메뉴는 연결 대상을 지정해야 합니다.")
    public boolean isTargetValid() {
        if (type == null) {
            return true;
        }
        return switch (type) {
            case LINK -> link != null && !link.isBlank();
            case FEATURE -> true;
            default -> targetId != null;
        };
    }
}
